package es.deusto.spq.deustocrai.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotificacion(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom("deustocrai@gmail.com"); 
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            mailSender.send(email);
        } catch (Exception e) {
            // Evitamos que falle todo el sistema de reservas si hay un problema temporal con el email
            System.err.println("Error al enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}	