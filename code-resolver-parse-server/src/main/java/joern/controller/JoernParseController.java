package joern.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import joern.service.JoernParseServiceImpl;

@RequestMapping("/joern")
@Controller
public class JoernParseController {
    @Autowired
    private JoernParseServiceImpl joernParseService;
    @GetMapping("/parse")
    @ResponseBody
    public String parseAndImport(@RequestParam("url") String url){
        return joernParseService.parse(url);
    }
}
