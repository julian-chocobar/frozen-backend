package materials.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import materials.DTO.MaterialFilterDTO;
import materials.DTO.MaterialDTO;

public interface MaterialService {

    Page<MaterialDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable);
}
