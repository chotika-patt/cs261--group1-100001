package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ReviewImageDto;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.models.Review;
import tu_store.demo.models.ReviewImage;
import tu_store.demo.models.User;
import tu_store.demo.repositories.OrderRepository;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service

public class ReviewImageService {
    public ReviewImage creatReviewImage(Review review, ReviewImageDto imageDto){
        if(review == null || imageDto == null) return null;

        ReviewImage reImage = new ReviewImage();
        reImage.setFilePath(imageDto.getFilePath());
        reImage.setMimeType(imageDto.getMimeType());
        reImage.setSize(imageDto.getSize());
        reImage.setReview(review);
        return reImage;
    }
}
