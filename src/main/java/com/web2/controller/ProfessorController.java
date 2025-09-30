package com.web2.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web2.dto.ProfessorDTO;
import com.web2.model.Professor;
import com.web2.repository.ProfessorRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/professor")
public class ProfessorController {
	@Autowired
	ProfessorRepository repository;
	
	@GetMapping("/inserir")
	public String inserir() {
		return "professor/inserir";
	}
	
	@PostMapping("/inserir")
	public String inserido(
			@ModelAttribute @Valid ProfessorDTO dto,	 
			BindingResult result, 
			RedirectAttributes msg,
			@RequestParam("file") MultipartFile imagem) {
		if(result.hasErrors()) {
			msg.addFlashAttribute("erro", "Erro ao inserir!");
			return "redirect:/professor/listar";
		}
		var professor = new Professor();		
		BeanUtils.copyProperties(dto, professor);
		try {
			if(!imagem.isEmpty()) {
				byte[] bytes = imagem.getBytes();
				
				Path caminho = Paths.get(
						"./src/main/resources/static/img/"+
								imagem.getOriginalFilename());
				
				Files.write(caminho, bytes);
				professor.setImagem(imagem.getOriginalFilename());
			}
		}catch(IOException e) {
			System.out.println("erro imagem");
		}
		repository.save(professor);
		msg.addFlashAttribute("inserirok", "Professor inserido!");
		return "redirect:/professor/listar";
	}
	
	@GetMapping("/imagem/{imagem}")
	@ResponseBody
	public byte[] mostraImagem(@PathVariable("imagem") String imagem) 
			throws IOException {
		File nomeArquivo = 
				new File("./src/main/resources/static/img/"+imagem);
		// CORREÇÃO LÓGICA: Usando '&&' para garantir que a string não é nula E não está vazia.
		if(imagem != null && imagem.trim().length() > 0) {
			return Files.readAllBytes(nomeArquivo.toPath());
		}
		return null;
	}
		
	
	@GetMapping("/listar")
	public ModelAndView listar() {
		ModelAndView mv = new ModelAndView("/professor/listar");
		List<Professor> lista = repository.findAll();
		// CORREÇÃO: Enviando a lista com o nome no plural "professores"
		mv.addObject("professores", lista);
		return mv;
	}
	@PostMapping("/listar")
	public ModelAndView listarprofessorFind
	(@RequestParam("busca") String buscar){
		ModelAndView mv = new ModelAndView("professor/listar");
		List<Professor> lista = 
				repository.findProfessorByNomeLike("%"+buscar+"%");
		// CORREÇÃO: Enviando a lista com o nome no plural "professores"
		mv.addObject("professores", lista);
		return mv;
	}
	
	@GetMapping("/excluir/{id}")
	public String excluir(@PathVariable(value="id") int id) {
		Optional<Professor> professor= repository.findById(id);
		if(professor.isEmpty()) {
			return "redirect:/professor/listar";			
		}
		repository.deleteById(id);
		return "redirect:/professor/listar";					
	}
	@GetMapping("/editar/{id}")
	public ModelAndView editar(@PathVariable(value="id") int id) {
		ModelAndView mv = new ModelAndView("/professor/editar");
		// CORREÇÃO: Renomeando variável para "professor" para clareza
		Optional<Professor> professor= repository.findById(id);
		mv.addObject("id", professor.get().getId());
		mv.addObject("nome", professor.get().getNome());
		mv.addObject("email", professor.get().getEmail());
		return mv;
	}
	@PostMapping("/editar/{id}")
	public String editado(
			@ModelAttribute @Valid ProfessorDTO dto, 
			BindingResult result, 
			RedirectAttributes msg,
			@PathVariable(value="id") int id) {
		if(result.hasErrors()) {
			msg.addFlashAttribute("erro", "Erro ao editar!");
			return "redirect:/professor/listar";
		}
		Optional<Professor> professor= repository.findById(id);
		var professor2 = professor.get();
		BeanUtils.copyProperties(dto, professor2);
		repository.save(professor2);
		msg.addFlashAttribute("sucesso", "Professor editado!");
		return "redirect:/professor/listar";
	}
	
}