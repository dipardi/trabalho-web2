package com.web2.controller;

import com.web2.model.Categoria;
import com.web2.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("home");
        // Busca todas as categorias no banco para enviar para a view
        List<Categoria> categorias = categoriaRepository.findAll();
        mv.addObject("categorias", categorias);
        return mv;
    }
}