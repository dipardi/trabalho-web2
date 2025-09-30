package com.web2.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfessorDTO(
		@NotBlank String nome, 
		@NotBlank String email
		) {

}
