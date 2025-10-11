package materials.service;

import lombok.RequiredArgsConstructor;
import materials.mapper.MaterialMapper;
import materials.repository.MaterialRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService{

    final MaterialRepository materialRepository;
    final MaterialMapper materialMapper;
}
