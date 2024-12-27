package io.tekno.contactapi.service;

import io.tekno.contactapi.domain.Contact;
import io.tekno.contactapi.repo.ContactRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.tekno.contactapi.constant.Constant.PHOTO_DIR;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepo contactRepo;

    public Page<Contact> getAllContacts(int page, int size){
       return contactRepo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id){
        return contactRepo.findById(id).orElseThrow(() -> new RuntimeException("Contact Not Found"));
    }

    public Contact createContact(Contact contact){
        return contactRepo.save(contact);
    }

    public void deleteContact(Contact contact){
        contactRepo.delete(contact);
    }

    public String uploadPhoto(String id, MultipartFile file){
        Contact contact = getContact(id);
        String photoUrl = photoFunc(id, file);
        contact.setPicUrl(photoUrl);
        contactRepo.save(contact);
        return photoUrl;
    }

    private final String fileExtensionOf(String filename) {
        if(filename.contains(".")){
            return filename.substring(filename.lastIndexOf("." + 1));
        }
        return ".png";
    }

    private final String photoFunc(String id, MultipartFile image) {
        try{
            Path fileLoc = Paths.get(PHOTO_DIR).toAbsolutePath().normalize();
            if(!Files.exists(fileLoc)){Files.createDirectories(fileLoc);}
            String ext = fileExtensionOf(image.getOriginalFilename());
            Files.copy(image.getInputStream(), fileLoc.resolve(id + ext), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image" + id + ext)
                    .toUriString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
