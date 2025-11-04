package ers.backend.service.cloud;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("api_secret", "h99WAcZ0qX0v88d-9q1nDWiSYsw");
        valuesMap.put("api_key", "867627382471163");
        valuesMap.put("cloud_name", "ddci2vkeu");
        cloudinary = new Cloudinary(valuesMap);
    }

    public Map upload(MultipartFile multipartFile ) throws IOException {
        File file = convert(multipartFile);
        System.out.println("2) We have converted the multipart file");
        Map result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        System.out.println("3) We have uploaded the image!");
        if(!Files.deleteIfExists(file.toPath())) {
            throw new IOException("Failed to delete temporary file: " + file.getAbsolutePath());
        }
        return result;
    }

    public void delete(String id) throws IOException {
         cloudinary.uploader().destroy(id, ObjectUtils.emptyMap());
        System.out.println("Image Deleted!");
    }

    private File convert(MultipartFile multipartFile) throws IOException {

        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(multipartFile.getBytes());
        fo.close();
        return file;
    }
}
