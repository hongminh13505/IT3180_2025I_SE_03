package com.apartment.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/admin/thong-bao")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class ThongBaoController {
    
    @GetMapping
    public String list() {
        return "redirect:/admin/thong-bao-2";
    }
    
    @GetMapping("/create")
    public String createForm() {
        return "redirect:/admin/thong-bao-2/create";
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id) {
        return "redirect:/admin/thong-bao-2/edit/" + id;
    }
    
    @GetMapping("/**")
    public String catchAll() {
        return "redirect:/admin/thong-bao-2";
    }
}


