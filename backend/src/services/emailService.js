const nodemailer = require('nodemailer');

const EMAIL_USER = process.env.SMTP_EMAIL || 'limaenaccionsoporte@gmail.com';
const EMAIL_PASS = process.env.SMTP_PASS || 'mdyx ivba kbmw vtyl';

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: EMAIL_USER,
        pass: EMAIL_PASS
    }
});

/**
 * Envía el código OTP de verificación para crear cuenta ciudadana con validez de 1 minuto.
 */
async function enviarCodigoVerificacionRegistro(destinatario, nombre, codigo) {
    const htmlContent = `
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Código de Activación - Lima en Acción</title>
        <style>
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6; margin: 0; padding: 20px; color: #1f2937; }
            .container { max-width: 520px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
            .header { background: linear-gradient(135deg, #15803d 0%, #166534 100%); padding: 32px 24px; text-align: center; color: #ffffff; }
            .header h1 { margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 0.5px; }
            .header p { margin: 6px 0 0; font-size: 14px; opacity: 0.9; }
            .content { padding: 32px 24px; }
            .saludo { font-size: 16px; margin-bottom: 16px; }
            .info { font-size: 14px; line-height: 1.6; color: #4b5563; margin-bottom: 24px; }
            .code-box { background-color: #f0fdf4; border: 2px dashed #22c55e; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px; }
            .code-box .code { font-size: 38px; font-weight: 800; letter-spacing: 8px; color: #15803d; font-family: monospace; }
            .code-box .expiry { font-size: 13px; color: #166534; margin-top: 8px; font-weight: 600; }
            .warning { background-color: #eff6ff; border-left: 4px solid #3b82f6; padding: 12px 16px; border-radius: 6px; font-size: 13px; color: #1e40af; margin-bottom: 24px; line-height: 1.5; }
            .footer { background-color: #f9fafb; padding: 20px 24px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #e5e7eb; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>🛡️ Lima en Acción</h1>
                <p>Verificación de Nueva Cuenta Ciudadana</p>
            </div>
            <div class="content">
                <div class="saludo">Hola <strong>${nombre || 'Vecino'}</strong>,</div>
                <div class="info">
                    Gracias por unirte a la red de seguridad de <strong>Lima en Acción</strong>. Para confirmar la veracidad de tus datos y completar tu registro vecinal, ingresa el siguiente código de 6 dígitos en la aplicación:
                </div>
                <div class="code-box">
                    <div class="code">${codigo}</div>
                    <div class="expiry">⏱️ Válido durante 1 minuto (60 segundos)</div>
                </div>
                <div class="warning">
                    ℹ️ <strong>Importante:</strong> Pasado el minuto, deberás pulsar "Reenviar código" para generar uno nuevo por motivos de seguridad.
                </div>
            </div>
            <div class="footer">
                Este es un mensaje automático de seguridad vecinal enviado por Lima en Acción.<br>
                Lima - Perú
            </div>
        </div>
    </body>
    </html>
    `;

    const mailOptions = {
        from: '"Lima en Acción · Registro" <soportelimaenaccion@gmail.com>',
        to: destinatario,
        subject: `🔐 ${codigo} es tu código de activación - Lima en Acción`,
        text: `Hola ${nombre || 'Vecino'},\n\nTu código de verificación para Lima en Acción es: ${codigo}\n\nVálido durante 1 minuto.`,
        html: htmlContent
    };

    return await transporter.sendMail(mailOptions);
}

/**
 * Envía el código OTP de recuperación de contraseña con plantilla HTML profesional.
 */
async function enviarCodigoRecuperacion(destinatario, nombre, codigo) {
    const htmlContent = `
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Recuperación de Contraseña - Lima en Acción</title>
        <style>
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f3f4f6; margin: 0; padding: 20px; color: #1f2937; }
            .container { max-width: 520px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
            .header { background: linear-gradient(135deg, #15803d 0%, #166534 100%); padding: 32px 24px; text-align: center; color: #ffffff; }
            .header h1 { margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 0.5px; }
            .header p { margin: 6px 0 0; font-size: 14px; opacity: 0.9; }
            .content { padding: 32px 24px; }
            .saludo { font-size: 16px; margin-bottom: 16px; }
            .info { font-size: 14px; line-height: 1.6; color: #4b5563; margin-bottom: 24px; }
            .code-box { background-color: #f0fdf4; border: 2px dashed #22c55e; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px; }
            .code-box .code { font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #15803d; font-family: monospace; }
            .code-box .expiry { font-size: 12px; color: #166534; margin-top: 8px; font-weight: 600; }
            .warning { background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 12px 16px; border-radius: 6px; font-size: 13px; color: #92400e; margin-bottom: 24px; line-height: 1.5; }
            .footer { background-color: #f9fafb; padding: 20px 24px; text-align: center; font-size: 12px; color: #9ca3af; border-top: 1px solid #e5e7eb; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1>🛡️ Lima en Acción</h1>
                <p>Seguridad y Vigilancia Vecinal Comunitaria</p>
            </div>
            <div class="content">
                <div class="saludo">Hola <strong>${nombre || 'Vecino'}</strong>,</div>
                <div class="info">
                    Recibimos una solicitud para restablecer la contraseña de acceso a tu cuenta ciudadana en la aplicación móvil de <strong>Lima en Acción</strong>.
                </div>
                <div class="code-box">
                    <div class="code">${codigo}</div>
                    <div class="expiry">⏱️ Válido por 1 minuto (un solo uso)</div>
                </div>
                <div class="warning">
                    ⚠️ <strong>Aviso de Seguridad:</strong> Si tú no solicitaste este cambio, no te preocupes; tu cuenta sigue protegida. Puedes ignorar este mensaje o reportarlo a soporte.
                </div>
            </div>
            <div class="footer">
                Este es un mensaje automático de seguridad enviado por la plataforma Lima en Acción.<br>
                Lima - Perú
            </div>
        </div>
    </body>
    </html>
    `;

    const mailOptions = {
        from: '"Lima en Acción · Soporte" <soportelimaenaccion@gmail.com>',
        to: destinatario,
        subject: `🔐 ${codigo} es tu código de recuperación - Lima en Acción`,
        text: `Hola ${nombre || 'Vecino'},\n\nTu código de recuperación para Lima en Acción es: ${codigo}\n\nVálido por 1 minuto.\n\nSi no solicitaste este código, ignora este mensaje.`,
        html: htmlContent
    };

    return await transporter.sendMail(mailOptions);
}

module.exports = {
    enviarCodigoVerificacionRegistro,
    enviarCodigoRecuperacion
};
