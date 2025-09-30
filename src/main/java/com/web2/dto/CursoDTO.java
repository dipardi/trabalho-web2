package com.web2.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CursoDTO(
        @NotBlank String nome,
        String descricao,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFinal,
        @NotNull Integer professor_id,
        @NotNull Integer categoria_id
) {
}