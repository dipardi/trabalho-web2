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

import com.web2.dto.UsuarioDTO;
import com.web2.model.Usuario;
import com.web2.repository.UsuarioRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {
	@Autowired
	UsuarioRepository repository;
	
	// Pega o diretório raiz do projeto e constrói o caminho absoluto para as imagens.
	private static Path caminhoImagens = Paths.get(System.getProperty("user.dir"), "/src/main/resources/static/img");

	@GetMapping("/inserir")
	public String inserir() {
		return "usuario/inserir";
	}
	
	@PostMapping("/inserir")
	public String inserido(
			@ModelAttribute @Valid UsuarioDTO dto,	 
			BindingResult result, 
			RedirectAttributes msg,
			@RequestParam("file") MultipartFile imagem) {

		if(result.hasErrors()) {
			msg.addFlashAttribute("erro", "Erro de validação ao inserir!");
			return "redirect:/usuario/listar";
		}
		var usuario = new Usuario();		
		BeanUtils.copyProperties(dto, usuario);
		try {
			if(!imagem.isEmpty()) {
				// --- SOLUÇÃO DEFINITIVA APLICADA AQUI ---
				// Garante que o diretório de destino exista antes de salvar.
				if (!Files.exists(caminhoImagens)) {
					Files.createDirectories(caminhoImagens);
				}

				String nomeOriginal = imagem.getOriginalFilename();
				Path caminhoArquivo = caminhoImagens.resolve(nomeOriginal);
				
				byte[] bytes = imagem.getBytes();
				Files.write(caminhoArquivo, bytes);

				usuario.setImagem(nomeOriginal);
			}
		}catch(IOException e) {
			System.err.println("!!! ERRO CRÍTICO AO SALVAR IMAGEM !!!");
			e.printStackTrace();
		}
		repository.save(usuario);
		msg.addFlashAttribute("inserirok", "Usuário inserido!");
		return "redirect:/usuario/listar";
	}
	
	@GetMapping("/imagem/{imagem}")
	@ResponseBody
	public byte[] mostraImagem(@PathVariable("imagem") String imagem) 
			throws IOException {
		Path caminhoArquivo = caminhoImagens.resolve(imagem);
		File nomeArquivo = caminhoArquivo.toFile();

		if(imagem != null && !imagem.trim().isEmpty() && nomeArquivo.exists()) {
			return Files.readAllBytes(nomeArquivo.toPath());
		}
		return null;
	}
		
	// --- O resto do controller permanece o mesmo ---
	
	@GetMapping("/listar")
	public ModelAndView listar() {
		ModelAndView mv = new ModelAndView("/usuario/listar");
		List<Usuario> lista = repository.findAll();
		mv.addObject("usuarios", lista);
		return mv;
	}

	@PostMapping("/listar")
	public ModelAndView listarusuariosFind
	(@RequestParam("busca") String buscar){
		ModelAndView mv = new ModelAndView("usuario/listar");
		List<Usuario> lista = 
				repository.findUsuarioByNomeLike("%"+buscar+"%");
		mv.addObject("usuarios", lista);
		return mv;
	}
	
	@GetMapping("/excluir/{id}")
	public String excluir(@PathVariable(value="id") int id) {
		Optional<Usuario> usuario= repository.findById(id);
		if(usuario.isEmpty()) {
			return "redirect:/usuario/listar";			
		}
		repository.deleteById(id);
		return "redirect:/usuario/listar";					
	}

	@GetMapping("/editar/{id}")
	public ModelAndView editar(@PathVariable(value="id") int id) {
		ModelAndView mv = new ModelAndView("/usuario/editar");
		Optional<Usuario> usuario= repository.findById(id);
		mv.addObject("id", usuario.get().getId());
		mv.addObject("nome", usuario.get().getNome());
		mv.addObject("email", usuario.get().getEmail());
		mv.addObject("senha", usuario.get().getSenha());
		return mv;
	}
	
	@PostMapping("/editar/{id}")
	public String editado(
			@ModelAttribute @Valid UsuarioDTO dto, 
			BindingResult result, 
			RedirectAttributes msg,
			@PathVariable(value="id") int id) {
		if(result.hasErrors()) {
			msg.addFlashAttribute("erro", "Erro ao editar!");
			return "redirect:/usuario/listar";
		}
		Optional<Usuario> usuario= repository.findById(id);
		var usuario2 = usuario.get();
		BeanUtils.copyProperties(dto, usuario2);
		repository.save(usuario2);
		msg.addFlashAttribute("sucesso", "Usuário editado!");
		return "redirect:/usuario/listar";
	}
}