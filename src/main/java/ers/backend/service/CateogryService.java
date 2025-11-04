package ers.backend.service;

import ers.backend.model.Category;
import ers.backend.repo.CategoryRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CateogryService {

    private final CategoryRepo categoryRepo;


    public CateogryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }


    public Category save(Category category) {
        return categoryRepo.save(category);
    }

    public List<Category> getAll() {
        return categoryRepo.findAll();
    }

    public void delete(Long categoryId) {
        categoryRepo.deleteById(categoryId);
    }

    public Category getById(Long categoryId) throws NoSuchElementException {
        return categoryRepo.findById(categoryId).get();
    }

    public boolean existsByName(String categoryName) {
        return categoryRepo.existsByCategoryName(categoryName);
    }
}
