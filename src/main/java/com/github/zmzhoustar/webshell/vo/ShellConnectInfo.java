/*
 * Copyright © 2020-present zmzhou-star. All Rights Reserved.
 */

package com.github.zmzhoustar.webshell.vo;

import lombok.Data;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;

/**
 * ssh连接信息
 *
 * @author zmzhou
 * @version 1.0
 * @title SSHConnectInfo
 * @date 2021/2/23 21:05
 */
@Data
public class ShellConnectInfo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1555506471798748444L;
    /**
     * WebSocketSession
     */
    private WebSocketSession webSocketSession;
    /**
     * JSch是SSH2的一个纯Java实现
     */
    private SSHClient ssh;
    /**
     * session
     */
    private Session session;

    private Session.Shell shell;
}
