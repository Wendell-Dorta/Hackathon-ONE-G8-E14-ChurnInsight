package com.hackathon.databeats.churninsight.infra.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Utilitário para operações de rede e extração de dados HTTP.
 *
 * <p>Fornece métodos auxiliares para análise de requisições HTTP, especialmente
 * útil em ambientes com proxies reversos e load balancers.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public class NetworkUtils {

	private NetworkUtils() {}

	/**
	 * Aceita apenas IPv4 (nnn.nnn.nnn.nnn) e IPv6 compacto (sem port/zone).
	 * Usado para validar IPs extraídos de headers antes de registrá-los.
	 */
	private static final Pattern VALID_IP = Pattern.compile(
			"^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$" +
			"|^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{0,4}){2,7}$");

	/**
	 * Extrai o endereço IP real do cliente de uma requisição HTTP.
	 *
	 * <p><b>Estratégia de extração:</b></p>
	 * <ol>
	 *   <li>Verifica header {@code X-Forwarded-For} — usa o IP mais à direita (adicionado pelo
	 *       proxy confiável imediatamente à frente desta aplicação), que não pode ser forjado
	 *       pelo cliente.</li>
	 *   <li>Verifica header {@code X-Real-IP} (Nginx).</li>
	 *   <li>Retorna {@code RemoteAddr} da conexão TCP (fallback).</li>
	 *   <li>Retorna "0.0.0.0" se requisição é null.</li>
	 * </ol>
	 *
	 * <p><b>Pré-requisito:</b> Esta aplicação DEVE estar atrás de exatamente um proxy reverso
	 * confiável. Se houver mais de um proxy, ajuste o índice conforme a quantidade de hops.</p>
	 *
	 * @param request requisição HTTP
	 * @return endereço IP do cliente ou "0.0.0.0" se indisponível
	 */
	public static String getClientIp(HttpServletRequest request) {
		if (request == null) {
			return "0.0.0.0";
		}

		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(xForwardedFor)) {
			// O IP mais à direita é adicionado pelo proxy confiável e não pode ser falsificado
			// pelo cliente. O lado esquerdo é inteiramente controlado pelo requisitante.
			String[] parts = xForwardedFor.split(",");
			String candidate = parts[parts.length - 1].trim();
			if (VALID_IP.matcher(candidate).matches()) {
				return candidate;
			}
		}

		String xRealIp = request.getHeader("X-Real-IP");
		if (StringUtils.hasText(xRealIp)) {
			String candidate = xRealIp.trim();
			if (VALID_IP.matcher(candidate).matches()) {
				return candidate;
			}
		}

		return request.getRemoteAddr();
	}
}