/*
 * Copyright © 2020-present zmzhou-star. All Rights Reserved.
 */

package com.github.zmzhoustar.webshell.utils;

import com.github.zmzhoustar.webshell.Constants;
import com.github.zmzhoustar.webshell.vo.WebShellData;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SFTP服务器工具类
 * SftpUtils
 *
 * @author zmzhou
 * @version 1.0
 * @date 2021/2/25 11:46
 */
@Slf4j
public final class SftpUtils {

    private SFTPClient sftp;
//    private Session session;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 秘钥
     */
    private String privateKey;

    /**
     * FTP服务器Ip
     */
    private final String host;

    /**
     * FTP服务器端口号
     */
    private final int port;

    /**
     * 构造器 基于密码认证
     *
     * @param sshData 用户名，密码，主机，端口
     * @author zmzhou
     * @date 2021/3/3 15:16
     */
    public SftpUtils(WebShellData sshData) {
        this.username = sshData.getUsername();
        this.password = "xxxxxx";
        this.host = sshData.getHost();
        this.port = sshData.getPort();
    }

    /**
     * 构造器：基于秘钥认证sftp对象
     *
     * @param username   用户名
     * @param privateKey 秘钥
     * @param host       服务器ip
     * @param port       服务器端口号
     */
    public SftpUtils(String username, String privateKey, int port, String host) {
        this.username = username;
        this.privateKey = privateKey;
        this.host = host;
        this.port = port;
    }

    /**
     * 连接SFTP服务器
     *
     * @return 连接成功
     * @author zmzhou
     * @date 2021/3/1 14:01
     */
    public boolean login() {
        try {
            SSHClient ssh = new SSHClient();
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect("localhost");
            ssh.authPassword("fangxiaoming", "xxxxxx");
            sftp = ssh.newSFTPClient();
            log.info("sftp server connect success !!");
        } catch (IOException e) {
            log.error("SFTP服务器连接异常！！", e);
            return false;
        }
        return true;
    }

    /**
     * 关闭SFTP连接
     */
    public void logout() {
//        if (channelSftp != null && channelSftp.isConnected()) {
//            channelSftp.disconnect();
//            log.debug("sftp closed");
//        }
//        if (session != null && session.isConnected()) {
//            session.disconnect();
//            log.debug("session closed");
//        }
    }

    /**
     * 将输入流上传到SFTP服务器，作为文件
     *
     * @param directory    上传到SFTP服务器的路径
     * @param sftpFileName 上传到SFTP服务器后的文件名
     * @param input        输入流
     * @throws SftpException SftpException
     * @author zmzhou
     * @date 2021/3/2 23:36
     */
//    public void upload(String directory, String sftpFileName, InputStream input) throws IOException {
//        long start = System.currentTimeMillis();
//        // 创建不存在的文件夹，并切换到文件夹
//        createDir(directory);
//        // 上传文件
//        sftp.put(input, sftpFileName);
//        log.info("文件上传成功！！ 耗时：{}ms", (System.currentTimeMillis() - start));
//    }

    /**
     * 下载文件
     *
     * @param path SFTP服务器的文件路径
     * @return 输入流
     * @author zmzhou
     * @date 2021/3/2 21:20
     */
    public FileSystemFile download(String path) {
        // 文件所在目录
        String directory = path.substring(0, path.lastIndexOf(Constants.SEPARATOR));
        // 文件名
        String fileName = path.substring(path.lastIndexOf(Constants.SEPARATOR) + 1);
        return download(directory, fileName);
    }

    /**
     * 下载文件
     *
     * @param directory SFTP服务器的文件路径
     * @param fileName  SFTP服务器上的文件名
     * @return 输入流
     * @author zmzhou
     * @date 2021/3/2 21:20
     */
    public FileSystemFile download(String directory, String fileName) {
        try {

            log.info("下载文件:{}/{}", directory, fileName);
            FileSystemFile fileSystemFile = new FileSystemFile(directory);
            sftp.get(fileName, fileSystemFile);
//            sftp.rm();
            return fileSystemFile;
        } catch (IOException e) {
            log.error("下载文件:{}/{}异常！", directory, fileName, e);
        }
        return null;
    }

    /**
     * 删除文件或者空文件夹
     *
     * @param directory SFTP服务器的文件路径
     * @param fileName  删除的文件名称
     * @return 删除结果
     * @author zmzhou
     * @date 2021/3/4 21:47
     */
//    private boolean delete(String directory, String fileName) {
//        String file = directory + Constants.SEPARATOR + fileName;
//        try {
//            ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) listFiles(file).get(0);
//            // 用户权限处理
//            if (!(Constants.USER_ROOT.equals(username)
//                || username.equals(SftpFileUtils.getOwner(lsEntry.getLongname())))) {
//                log.warn("用户{}没有权限删除文件：{}", username, file);
//                return false;
//            }
//            channelSftp.cd(directory);
//            if (isDirExists(file)) {
//                // 删除空文件夹
//                channelSftp.rmdir(fileName);
//            } else {
//                channelSftp.rm(fileName);
//            }
//            log.info("删除文件：{}成功", file);
//        } catch (SftpException e) {
//            log.error("删除文件异常：{}", file, e);
//            return false;
//        }
//        return true;
//    }

    /**
     * 删除文件或者文件夹
     *
     * @param path SFTP服务器的文件或者文件夹路径
     * @return 删除结果
     * @author zmzhou
     * @date 2021/3/4 21:47
     */
//    public boolean delete(String path) {
//        AtomicBoolean delFlag = new AtomicBoolean(true);
//        Vector<?> vector = listFiles(path);
//        // 是文件或者空文件夹
//        if (isFileExists(path) || vector.isEmpty()) {
//            // 文件所在目录
//            String directory = path.substring(0, path.lastIndexOf(Constants.SEPARATOR));
//            // 文件名
//            String fileName = path.substring(path.lastIndexOf(Constants.SEPARATOR) + 1);
//            return delete(directory, fileName);
//        } else if (isDirExists(path)) {
//            // 1.先循环删除子文件
//            vector.forEach(v -> {
//                ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry) v;
//                // 如果是文件夹，递归删除
//                if (FileType.DIRECTORY.getSign().equals(lsEntry.getLongname().substring(0, 1))) {
//                    delFlag.set(delete(path + Constants.SEPARATOR + lsEntry.getFilename()));
//                } else {
//                    // 删除文件
//                    delFlag.set(delete(path, lsEntry.getFilename()));
//                }
//            });
//            // 2.再删除空文件夹
//            delFlag.set(delete(path));
//        }
//        return delFlag.get();
//    }

    /**
     * 获取文件夹下的文件列表
     *
     * @param directory 路径
     * @return 文件列表
     * @author zmzhou
     * @date 2021/3/1 9:56
     */
    public List<RemoteResourceInfo> listFiles(String directory) {
        try {
            if (isDirExists(directory) || isFileExists(directory)) {
                List<RemoteResourceInfo> files = sftp.ls(directory);
                //移除上级目录和根目录："." ".."
                Iterator<?> it = files.iterator();
                while (it.hasNext()) {
                    RemoteResourceInfo remoteResourceInfo = (RemoteResourceInfo) it.next();
                    if (Constants.DOT.equals(remoteResourceInfo.getName())
                        || Constants.PARENT_DIRECTORY.equals(remoteResourceInfo.getName())) {
                        it.remove();
                    }
                }
                return files;
            }
        } catch (IOException e) {
            log.error("获取文件夹信息异常！", e);
        }
        return new ArrayList<>();
    }

    /**
     * 判断目录是否存在，不存在则创建，并进入目录
     *
     * @param createPath 路径
     * @return 创建成功并进入目录
     * @author zmzhou
     * @date 2021/3/3 10:53
     */
//    public boolean createDir(String createPath) {
//        try {
//            if (isDirExists(createPath)) {
//                this.channelSftp.cd(createPath);
//                return true;
//            }
//            String[] pathArray = createPath.split(Constants.SEPARATOR);
//            StringBuilder filePath = new StringBuilder(Constants.SEPARATOR);
//            for (String path : pathArray) {
//                if ("".equals(path)) {
//                    continue;
//                }
//                filePath.append(path);
//                // 路径如果是文件，跳过，保存到同级目录
//                if (isFileExists(filePath.toString())) {
//                    continue;
//                }
//                filePath.append(Constants.SEPARATOR);
//                if (!isDirExists(filePath.toString())) {
//                    // 建立目录
//                    channelSftp.mkdir(filePath.toString());
//                }
//                // 并进入目录
//                channelSftp.cd(filePath.toString());
//            }
//        } catch (SftpException e) {
//            log.error("目录创建异常！", e);
//            return false;
//        }
//        return true;
//    }

    /**
     * 判断目录是否存在
     *
     * @param directory 路径
     * @return 目录是否存在
     * @author zmzhou
     * @date 2021/3/3 11:04
     */
    public boolean isDirExists(String directory) {
        try {
            FileAttributes lstat = this.sftp.lstat(directory);
            return null != lstat && lstat.getType() == FileMode.Type.DIRECTORY;
        } catch (Exception e) {
            log.error("判断目录是否存在异常：{}", directory, e);
        }
        return false;
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     * @author zmzhou
     * @date 2021/3/3 11:04
     */
    public boolean isFileExists(String filePath) {
        try {
            FileMode.Type type = this.sftp.type(filePath);
            // 存在并且不是文件夹
            return null != type && type != FileMode.Type.DIRECTORY;
        } catch (Exception e) {
            log.error("判断文件是否存在异常：{}", filePath, e);
        }
        return false;
    }
}
