package com.example.auditoria.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HallazgoJpaRepository extends JpaRepository<HallazgoJpaEntity, String> {

    @Query("""
            SELECT h.severidad AS categoria, COUNT(h) AS total
            FROM HallazgoJpaEntity h
            GROUP BY h.severidad
            ORDER BY h.severidad
            """)
    List<ConteoProjection> contarPorSeveridad();

    @Query("""
            SELECT h.estado AS categoria, COUNT(h) AS total
            FROM HallazgoJpaEntity h
            GROUP BY h.estado
            ORDER BY h.estado
            """)
    List<ConteoProjection> contarPorEstado();

    @Query(value = """
            SELECT area_responsable AS categoria,
                   AVG(DATEDIFF('DAY', fecha_deteccion, fecha_cierre)) AS promedio
            FROM hallazgos
            WHERE estado = 'CERRADO' AND fecha_cierre IS NOT NULL
            GROUP BY area_responsable
            ORDER BY area_responsable
            """, nativeQuery = true)
    List<PromedioProjection> promedioDiasCierrePorArea();

    interface ConteoProjection {
        String getCategoria();

        Long getTotal();
    }

    interface PromedioProjection {
        String getCategoria();

        Number getPromedio();
    }
}
