package com.jobhunt.saas.service;

import com.jobhunt.saas.dto.SubscriptionCategoryDto;
import com.jobhunt.saas.entity.SubscriptionCategory;
import com.jobhunt.saas.repository.SubscriptionCategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionCategoryService {

    private final SubscriptionCategoryRepo subscriptionCategoryRepo;

    public SubscriptionCategoryDto createSubscriptionCategory(SubscriptionCategoryDto categoryRequest){

        SubscriptionCategory subscriptionCategory = SubscriptionCategory.builder()
                .icon(categoryRequest.getIcon())
                .name(categoryRequest.getName())
                .build();

       SubscriptionCategory category=subscriptionCategoryRepo.save(subscriptionCategory);

       return new SubscriptionCategoryDto().builder()
               .id(category.getId())
               .name(category.getName())
               .build();
    }

    public List<SubscriptionCategoryDto> getAllSubscriptionCategories() {

        List<SubscriptionCategory> subscriptionCategories = subscriptionCategoryRepo.findAll();
        List<SubscriptionCategoryDto> subscriptionCategoryDtos = new ArrayList<>();

        //Convert entity to Dto
        for(SubscriptionCategory subscriptionCategory : subscriptionCategories){

            SubscriptionCategoryDto subscriptionCategoryDto = new SubscriptionCategoryDto();
            subscriptionCategoryDto.setId(subscriptionCategory.getId());
            subscriptionCategoryDto.setName(subscriptionCategory.getName());
            subscriptionCategoryDtos.add(subscriptionCategoryDto);

        }
        return subscriptionCategoryDtos;
    }


}
