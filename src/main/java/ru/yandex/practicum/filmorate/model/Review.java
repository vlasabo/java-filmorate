package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Review {

    @JsonProperty(value="reviewId")
    private int id;

    @NotNull
    private String content;

    @NotNull
    @JsonProperty(value="isPositive")
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int useful;

}
