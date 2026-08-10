package com.cyberrange.application.port.out;

import com.cyberrange.domain.catalog.ActionCatalog;

/**
 * Puerto de salida: de donde sale el catalogo de cartas. Que hoy sea un
 * fichero YAML es un detalle del adaptador.
 */
public interface ActionCatalogPort {

    ActionCatalog catalog();
}
