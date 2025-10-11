package materials.service;

import lombok.RequiredArgsConstructor;
import materials.mapper.MaterialMapper;
import materials.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import materials.specification.MaterialSpecification;
import materials.model.Material;
import materials.DTO.MaterialDTO;
import materials.DTO.MaterialFilterDTO;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService{

    final MaterialRepository materialRepository;
    final MaterialMapper materialMapper;

    @Override
    public Page<MaterialDTO> findAll(MaterialFilterDTO filterDTO, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort());
        Page<Material> materials = materialRepository.findAll(
                MaterialSpecification.createFilter(filterDTO), pageRequest);
        return materials.map(materialMapper::toDto);
    }
}
