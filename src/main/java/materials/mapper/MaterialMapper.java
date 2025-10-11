package materials.mapper;

import common.mapper.GlobalMapperConfig;
import materials.DTO.MaterialDTO;
import materials.model.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, componentModel = "spring")
public interface MaterialMapper {

    @Mapping(target = "unitMeasurement", ignore = true)
    MaterialDTO toDto(Material entity);
    
    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Material toEntity(MaterialDTO dto);
}
