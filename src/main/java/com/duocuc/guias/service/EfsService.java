package com.duocuc.guias.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

/**
 * Servicio para manejo de archivos temporales en EFS.
 * El EFS está montado en el contenedor Docker como volumen en app.efs.path.
 */
@Slf4j
@Service
public class EfsService {

    @Value("${app.efs.path}")
    private String efsBasePath;

    /**
     * Guarda un archivo PDF en el EFS de forma temporal.
     * Organiza los archivos en subcarpetas por transportista.
     *
     * @param contenido      bytes del PDF
     * @param transportista  nombre del transportista
     * @param numeroGuia     número único de la guía
     * @return ruta completa del archivo guardado en EFS
     */
    public String guardarEnEfs(byte[] contenido, String transportista, String numeroGuia) throws IOException {
        // Crear directorio por transportista si no existe
        String transportistaNormalizado = transportista.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        Path directorioTransportista = Paths.get(efsBasePath, transportistaNormalizado);
        Files.createDirectories(directorioTransportista);

        // Guardar el archivo
        String nombreArchivo = numeroGuia + ".pdf";
        Path rutaArchivo = directorioTransportista.resolve(nombreArchivo);
        Files.write(rutaArchivo, contenido, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        log.info("Archivo guardado en EFS: {}", rutaArchivo);
        return rutaArchivo.toString();
    }

    /**
     * Lee un archivo desde el EFS.
     *
     * @param rutaEfs ruta completa del archivo en EFS
     * @return bytes del archivo
     */
    public byte[] leerDesdeEfs(String rutaEfs) throws IOException {
        Path path = Paths.get(rutaEfs);
        if (!Files.exists(path)) {
            throw new NoSuchFileException("Archivo no encontrado en EFS: " + rutaEfs);
        }
        return Files.readAllBytes(path);
    }

    /**
     * Elimina un archivo del EFS una vez que ya fue subido a S3.
     *
     * @param rutaEfs ruta completa del archivo en EFS
     */
    public void eliminarDeEfs(String rutaEfs) {
        try {
            Path path = Paths.get(rutaEfs);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Archivo eliminado del EFS: {}", rutaEfs);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo del EFS {}: {}", rutaEfs, e.getMessage());
        }
    }

    /**
     * Verifica que el directorio EFS esté disponible y accesible.
     */
    public boolean efsDisponible() {
        Path base = Paths.get(efsBasePath);
        if (!Files.exists(base)) {
            try {
                Files.createDirectories(base);
                log.info("Directorio EFS creado: {}", efsBasePath);
            } catch (IOException e) {
                log.error("EFS no disponible en {}: {}", efsBasePath, e.getMessage());
                return false;
            }
        }
        return Files.isWritable(base);
    }
}
