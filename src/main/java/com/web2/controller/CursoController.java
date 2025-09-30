package com.web2.controller;

import com.web2.dto.CursoDTO;
import com.web2.model.Categoria;
import com.web2.model.Curso;
import com.web2.model.Professor;
import com.web2.repository.CategoriaRepository;
import com.web2.repository.CursoRepository;
import com.web2.repository.ProfessorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/curso")
public class CursoController {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/inserir")
    public ModelAndView inserir() {
        ModelAndView mv = new ModelAndView("curso/inserir");
        mv.addObject("curso", new Curso()); // Envia um objeto Curso vazio para o formulário
        mv.addObject("professores", professorRepository.findAll()); // Envia a lista de professores
        mv.addObject("categorias", categoriaRepository.findAll()); // Envia a lista de categorias
        return mv;
    }

    @PostMapping("/inserir")
    public String inserido(@ModelAttribute @Valid CursoDTO dto, BindingResult result,
                           RedirectAttributes msg, @RequestParam("file") MultipartFile imagem) {

        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao inserir! Verifique os campos obrigatórios.");
            // Redireciona de volta para o formulário de inserção para mostrar os erros
            return "redirect:/curso/inserir";
        }

        Curso curso = new Curso();
        BeanUtils.copyProperties(dto, curso);

        // Busca o professor e a categoria no banco de dados pelos IDs recebidos do DTO
        Optional<Professor> professorOpt = professorRepository.findById(dto.professor_id());
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.categoria_id());

        // Verifica se o professor ou a categoria foram encontrados
        if (professorOpt.isEmpty() || categoriaOpt.isEmpty()) {
            msg.addFlashAttribute("erro", "Professor ou Categoria inválida.");
            return "redirect:/curso/inserir";
        }

        // Associa o professor e a categoria ao curso
        curso.setProfessor(professorOpt.get());
        curso.setCategoria(categoriaOpt.get());

        // Lógica para salvar a imagem
        try {
            if (!imagem.isEmpty()) {
                byte[] bytes = imagem.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                Files.write(caminho, bytes);
                curso.setImagem(imagem.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar a imagem do curso.");
        }

        cursoRepository.save(curso);
        msg.addFlashAttribute("inserirok", "Curso inserido com sucesso!");
        return "redirect:/curso/listar";
    }

    @GetMapping("/listar")
    public ModelAndView listar() {
        ModelAndView mv = new ModelAndView("curso/listar");
        List<Curso> cursos = cursoRepository.findAll();
        mv.addObject("cursos", cursos);
        return mv;
    }
    
    @PostMapping("/listar")
    public ModelAndView listarPorNome(@RequestParam("busca") String busca) {
        ModelAndView mv = new ModelAndView("curso/listar");
        mv.addObject("cursos", cursoRepository.findByNomeContainingIgnoreCase(busca));
        return mv;
    }

    @GetMapping("/editar/{id}")
    public ModelAndView editar(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("curso/editar");
        Optional<Curso> cursoOpt = cursoRepository.findById(id);
        
        if (cursoOpt.isPresent()) {
            mv.addObject("curso", cursoOpt.get());
            mv.addObject("professores", professorRepository.findAll());
            mv.addObject("categorias", categoriaRepository.findAll());
        } else {
             mv.setViewName("redirect:/curso/listar"); // Se não encontrar o curso, redireciona para a lista
        }
        return mv;
    }

    @PostMapping("/editar/{id}")
    public String editado(@PathVariable("id") int id, @ModelAttribute @Valid CursoDTO dto, BindingResult result,
                          RedirectAttributes msg) {

        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Erro ao editar! Verifique os campos obrigatórios.");
            return "redirect:/curso/editar/" + id;
        }

        Optional<Curso> cursoOpt = cursoRepository.findById(id);
        if (cursoOpt.isEmpty()) {
            msg.addFlashAttribute("erro", "Curso não encontrado.");
            return "redirect:/curso/listar";
        }

        Curso curso = cursoOpt.get();
        BeanUtils.copyProperties(dto, curso);
        
        Optional<Professor> professorOpt = professorRepository.findById(dto.professor_id());
        Optional<Categoria> categoriaOpt = categoriaRepository.findById(dto.categoria_id());
        
        if (professorOpt.isEmpty() || categoriaOpt.isEmpty()) {
            msg.addFlashAttribute("erro", "Professor ou Categoria inválida.");
            return "redirect:/curso/editar/" + id;
        }

        curso.setProfessor(professorOpt.get());
        curso.setCategoria(categoriaOpt.get());

        cursoRepository.save(curso);
        msg.addFlashAttribute("editadook", "Curso editado com sucesso!");
        return "redirect:/curso/listar";
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable("id") int id, RedirectAttributes msg) {
        if (cursoRepository.existsById(id)) {
            cursoRepository.deleteById(id);
            msg.addFlashAttribute("excluidook", "Curso excluído com sucesso!");
        } else {
            msg.addFlashAttribute("erro", "Curso não encontrado para exclusão.");
        }
        return "redirect:/curso/listar";
    }
     @GetMapping("/imagem/{imagem}")
    @ResponseBody
    public byte[] mostraImagem(@PathVariable("imagem") String imagem) throws IOException {
        // Este caminho deve ser o mesmo usado no método de inserir/salvar a imagem
        File nomeArquivo = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null && imagem.trim().length() > 0) {
            return Files.readAllBytes(nomeArquivo.toPath());
        }
        return null;
    }
}