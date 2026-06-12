package fr.recia.esidoc.ws.model;

import lombok.Data;

@Data
public class EsidocError {

    String error;
    String error_description;
    String error_uri;

}
