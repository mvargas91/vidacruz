package com.mvargas.clinical_assistant.repositories;

import com.mvargas.enums.NivelTriaje;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Repository
@AllArgsConstructor
@Slf4j
public class EmergencyViewRepository {

    private final ReactiveStringRedisTemplate redis;

    static final String KEY_ACTIVOS = "urgencias:pacientes-activos";
    static final String KEY_CONTEO = "urgencias:conteo-por-nivel";

    /** KEYS[1]=conteo, KEYS[2]=activos; ARGV[1]=pacienteId, ARGV[2]=nivel. 1 si lo registró, 0 si ya existía. */
    private static final RedisScript<Long> REGISTRAR = RedisScript.of("""
            if redis.call('HSETNX', KEYS[2], ARGV[1], ARGV[2]) == 1 then
              redis.call('HINCRBY', KEYS[1], ARGV[2], 1)
              return 1
            end
            return 0
            """, Long.class);

    /** ARGV[1]=pacienteId. 1 si estaba activo y lo retiró, 0 si no estaba (alta repetida o desconocida). */
    private static final RedisScript<Long> ALTA = RedisScript.of("""
            local nivel = redis.call('HGET', KEYS[2], ARGV[1])
            if nivel then
              redis.call('HDEL', KEYS[2], ARGV[1])
              redis.call('HINCRBY', KEYS[1], nivel, -1)
              return 1
            end
            return 0
            """, Long.class);

    public Mono<Boolean> registrar(String patientId, NivelTriaje nivel) {
        return redis.execute(REGISTRAR, List.of(KEY_CONTEO, KEY_ACTIVOS), List.of(patientId, nivel.name()))
                .next()
                .map(r -> r == 1L);
    }

    public Mono<Boolean> darAlta(String patientId) {
        return redis.execute(ALTA, List.of(KEY_CONTEO, KEY_ACTIVOS), List.of(patientId))
                .next()
                .map(r -> r == 1L);
    }

    public Mono<Map<NivelTriaje, Long>> countByLevel() {
        log.info("started countByLevel");
        return redis.<String, String>opsForHash().entries(KEY_CONTEO)
                .collectList()
                .map(entradas -> {
                    Map<NivelTriaje, Long> resultado = new EnumMap<>(NivelTriaje.class);
                    for (NivelTriaje n : NivelTriaje.values()) {
                        resultado.put(n, 0L);
                    }
                    entradas.forEach(e -> {
                        try {
                            resultado.put(NivelTriaje.valueOf(e.getKey()), Math.max(0L, Long.parseLong(e.getValue())));
                        } catch (IllegalArgumentException __) {
                            log.error("terminated countByLevel");
                        }
                    });
                    return resultado;
                });
    }
}
