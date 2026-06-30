package com.isaacabarca.devops_dashboard.service;

import com.isaacabarca.devops_dashboard.dto.request.ServerCredentialRequest;
import com.isaacabarca.devops_dashboard.entity.Server;
import com.isaacabarca.devops_dashboard.entity.ServerCredential;
import com.isaacabarca.devops_dashboard.repository.ServerCredentialRepository;
import com.isaacabarca.devops_dashboard.repository.ServerRepository;
import com.isaacabarca.devops_dashboard.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServerCredentialService {

    private final ServerCredentialRepository credentialRepository;
    private final ServerRepository serverRepository;

    public void saveOrUpdate(Long serverId, ServerCredentialRequest request, String userEmail) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor no encontrado"));

        if (!server.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tienes permiso sobre este servidor");
        }

        String encryptedKey = EncryptionUtil.encrypt(request.getSshKey());

        Optional<ServerCredential> existing = credentialRepository.findByServerId(serverId);

        ServerCredential credential;
        if (existing.isPresent()) {
            credential = existing.get();
            credential.setSshUser(request.getSshUser());
            credential.setSshKey(encryptedKey);
            credential.setSshPort(request.getSshPort());
        } else {
            credential = ServerCredential.builder()
                    .sshUser(request.getSshUser())
                    .sshKey(encryptedKey)
                    .sshPort(request.getSshPort())
                    .server(server)
                    .build();
        }

        credentialRepository.save(credential);
    }
}