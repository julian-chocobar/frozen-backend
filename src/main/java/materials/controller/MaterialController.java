package materials.controller;


import lombok.RequiredArgsConstructor;
import materials.service.MaterialService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    final MaterialService materialService;
}
