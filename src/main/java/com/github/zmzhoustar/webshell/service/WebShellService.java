/*
 * Copyright © 2020-present zmzhou-star. All Rights Reserved.
 */

package com.github.zmzhoustar.webshell.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zmzhoustar.webshell.Constants;
import com.github.zmzhoustar.webshell.utils.ThreadPoolUtils;
import com.github.zmzhoustar.webshell.utils.WebShellUtils;
import com.github.zmzhoustar.webshell.vo.ShellConnectInfo;
import com.github.zmzhoustar.webshell.vo.WebShellData;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Web Shell业务逻辑实现
 *
 * @author zmzhou
 * @version 1.0
 * @title WebShellService
 * @date 2021/2/23 21:58
 */
@Slf4j
@Service
public class WebShellService {
    /**
     * 存放ssh连接信息的map
     */
    private static final Map<String, Object> SSH_MAP = new ConcurrentHashMap<>();

    /**
     * 初始化连接
     *
     * @param session WebSocketSession
     * @author zmzhou
     * @date 2021/2/23 21:22
     */
    public void initConnection(WebSocketSession session) {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ShellConnectInfo shellConnectInfo = new ShellConnectInfo();
        shellConnectInfo.setSsh(ssh);

        shellConnectInfo.setWebSocketSession(session);
        String uuid = WebShellUtils.getUuid(session);
        //将这个ssh连接信息放入缓存中
        SSH_MAP.put(uuid, shellConnectInfo);
    }

    /**
     * 处理客户端发送的数据
     *
     * @author zmzhou
     * @date 2021/2/23 21:21
     */
    public void recvHandle(String buffer, WebSocketSession session) {
        ObjectMapper objectMapper = new ObjectMapper();
        WebShellData shellData;
        try {
            shellData = objectMapper.readValue(buffer, WebShellData.class);
        } catch (IOException e) {
            log.error("Json转换异常:{}", e.getMessage());
            return;
        }
        String userId = WebShellUtils.getUuid(session);
        //找到刚才存储的ssh连接对象
        ShellConnectInfo shellConnectInfo = (ShellConnectInfo) SSH_MAP.get(userId);
        if (shellConnectInfo != null) {
            if (Constants.OPERATE_CONNECT.equals(shellData.getOperate())) {
                //启动线程异步处理
                ThreadPoolUtils.execute(() -> {
                    try {
                        connectToSsh(shellConnectInfo, shellData, session);
                        log.info("connect to {} success", shellData.getHost());
                    } catch (IOException e) {
                        log.error("web shell连接异常", e);
                        sendMessage(session, e.getMessage().getBytes());
                        close(session);
                    }
                });
            } else if (Constants.OPERATE_COMMAND.equals(shellData.getOperate())) {
                String command = shellData.getCommand();
                sendToTerminal(shellConnectInfo.getShell(), command);
            } else {
                log.error("不支持的操作");
                close(session);
            }
        }
    }

    /**
     * 关闭连接
     *
     * @param session WebSocketSession
     * @author zmzhou
     * @date 2021/2/23 21:16
     */
    public void close(WebSocketSession session) {
        String userId = WebShellUtils.getUuid(session);
        ShellConnectInfo shellConnectInfo = (ShellConnectInfo) SSH_MAP.get(userId);
        try {
            if (shellConnectInfo != null) {
                if (shellConnectInfo.getSession() != null) {
                    shellConnectInfo.getSession().close();
                }
                if (shellConnectInfo.getSsh() != null) {
                    shellConnectInfo.getSsh().disconnect();
                }
                //map中移除
                SSH_MAP.remove(userId);
            }
        } catch (Exception e) {
            log.error("close error", e);
        }
    }

    /**
     * 使用jsch连接终端
     *
     * @param shellConnectInfo ShellConnectInfo
     * @param sshData          WebShellData
     * @param webSocketSession WebSocketSession
     * @author zmzhou
     * @date 2021/2/23 21:15
     */
    private void connectToSsh(ShellConnectInfo shellConnectInfo, WebShellData sshData, WebSocketSession webSocketSession)
        throws IOException {
        //通道连接超时时间3s
        SSHClient ssh = shellConnectInfo.getSsh();
        //ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect("localhost");
        ssh.authPassword("fangxiaoming", "xxxxxx");
        Session session = ssh.startSession();
        session.allocateDefaultPTY();
        Session.Shell shell = session.startShell();

        shellConnectInfo.setSession(session);
        shellConnectInfo.setShell(shell);

        //查询上次登录时间
        sendToTerminal(shell, "lastlog -u " + sshData.getUsername() + "\r");
        try (InputStream inputStream = shell.getInputStream()) {
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int i;
            //如果没有数据来，线程会一直阻塞在这个地方等待数据。
            while ((i = inputStream.read(buffer)) != -1) {
                sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
            }
            log.info("login success=======================================");
        } catch (IOException e) {
            log.error("读取终端返回的信息流异常：", e);
        } finally {
            //断开连接后关闭会话
            log.info("close {} session", sshData.getUsername());
            session.close();
            ssh.disconnect();
        }
    }

    /**
     * 数据写回前端
     *
     * @param session WebSocketSession
     * @author zmzhou
     * @date 2021/2/23 21:18
     */
    public void sendMessage(WebSocketSession session, byte[] buffer) {
        try {
            log.info("send message {}", new String(buffer));
            session.sendMessage(new TextMessage(buffer));
        } catch (IOException e) {
            log.error("数据写回前端异常：", e);
        }
    }

    /**
     * 将消息转发到终端
     *
     * @param shell Shell
     * @author zmzhou
     * @date 2021/2/23 21:13
     */
    private void sendToTerminal(Session.Shell shell, String command) {
        if (shell != null) {
            try {
                OutputStream outputStream = shell.getOutputStream();
                outputStream.write(command.getBytes());
                outputStream.flush();
                log.info("send command {} success", command);
            } catch (IOException e) {
                log.error("web shell将消息转发到终端异常:{}", e.getMessage());
            }
        }
    }
}
