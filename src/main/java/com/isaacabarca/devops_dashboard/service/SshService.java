package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.entity.ServerCredential;
import com.isaacabarca.devops_dashboard.repository.ServerCredentialRepository;
import com.isaacabarca.devops_dashboard.util.EncryptionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshService {

    private final ServerCredentialRepository credentialRepository;

    public String executeCommand(Server server, String command) {
        ServerCredential credential = credentialRepository.findByServerId(server.getId())
                .orElseThrow(() -> new RuntimeException("Credenciales SSH no configuradas"));

        SSHClient ssh = new SSHClient();
        try {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(server.getIp(), credential.getSshPort());
            String decryptedKey = EncryptionUtil.decrypt(credential.getSshKey());
            ssh.authPassword(credential.getSshUser(), decryptedKey);

            Session session = ssh.startSession();
            Session.Command cmd = session.exec(command);
            String output = IOUtils.readFully(cmd.getInputStream()).toString();
            cmd.join();
            session.close();

            log.info("Comando ejecutado en {}: {}", server.getName(), command);
            return output;

        } catch (IOException e) {
            log.error("Error SSH en {}: {}", server.getName(), e.getMessage());
            throw new RuntimeException("Error al conectar por SSH: " + e.getMessage());
        } finally {
            try {
                ssh.disconnect();
            } catch (IOException ignored) {
            }
        }
    }

    public String getCpuUsage(Server server) {
        String result = executeCommand(server, "top -bn1 2>/dev/null | grep 'Cpu(s)' | awk '{print $2+$4}' || echo 'N/A'");
        return result.trim();
    }

    public String getRamUsage(Server server) {
        String result = executeCommand(server, "free -m 2>/dev/null | grep Mem | awk '{print ($3/$2)*100}' || echo 'N/A'");
        return result.trim();
    }

    public String getDiskUsage(Server server) {
        String result = executeCommand(server, "df -h / 2>/dev/null | tail -1 | awk '{print $5}' | sed 's/%//' || echo 'N/A'");
        return result.trim();
    }

    public String getDockerContainers(Server server) {
        return executeCommand(server, "docker ps --format '{{.ID}} {{.Names}} {{.Status}}' 2>/dev/null || echo 'Docker no instalado'");
    }

    public String getDockerLogs(Server server, String containerName) {
        return executeCommand(server, "docker logs --tail 50 " + containerName + " 2>/dev/null || echo 'Contenedor no encontrado'");
    }
    public String getSystemInfo(Server server) {
    return executeCommand(server, "uname -a && uptime && who");
    }

    public String getTopProcesses(Server server) {
        return executeCommand(server, "ps aux --sort=-%cpu | head -6");
    }

    public String getOpenPorts(Server server) {
        return executeCommand(server, "ss -tlnp 2>/dev/null || netstat -tlnp 2>/dev/null");
    }

    public String getSystemUpdates(Server server) {
        return executeCommand(server, "apt list --upgradable 2>/dev/null | head -10 || yum check-update 2>/dev/null | head -10");
    }
}