package materials.mapper;

import common.mapper.GlobalMapperConfig;
import materials.DTO.MaterialDTO;
import materials.model.Material;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface MaterialMapper {
    MaterialDTO toDto(Material entity);
    Material toEntity(MaterialDTO dto);
}
