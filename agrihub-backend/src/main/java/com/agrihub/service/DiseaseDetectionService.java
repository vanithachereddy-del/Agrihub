package com.agrihub.service;

import com.agrihub.model.DiseasePrediction;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Placeholder disease detection service.
 * <p>
 * Integrate a real TensorFlow / PyTorch / cloud vision model here by replacing
 * {@link #detect(MultipartFile)} with actual inference logic.
 */
@Service
public class DiseaseDetectionService {

    private static final List<DiseasePrediction> SAMPLE_PREDICTIONS = List.of(
            buildPrediction(
                    "Leaf Blight",
                    87.5,
                    "Leaf blight causes brown lesions on leaves, reducing photosynthesis and yield.",
                    List.of("Brown spots on leaves", "Yellow halos around lesions", "Premature leaf drop"),
                    List.of("Potassium-rich NPK", "Compost tea", "Micronutrient foliar spray"),
                    List.of("Copper oxychloride fungicide", "Mancozeb spray"),
                    List.of("Remove infected leaves", "Avoid overhead irrigation", "Ensure proper plant spacing")
            ),
            buildPrediction(
                    "Powdery Mildew",
                    82.3,
                    "White powdery patches appear on leaf surfaces, common in humid conditions.",
                    List.of("White powdery coating on leaves", "Curled or distorted leaves", "Stunted growth"),
                    List.of("Balanced NPK fertilizer", "Sulfur-based micronutrients"),
                    List.of("Sulfur fungicide", "Neem oil spray"),
                    List.of("Improve air circulation", "Water at soil level", "Prune dense foliage")
            ),
            buildPrediction(
                    "Bacterial Leaf Spot",
                    79.1,
                    "Dark water-soaked spots on leaves that may spread rapidly during warm wet weather.",
                    List.of("Dark water-soaked spots", "Yellowing leaf margins", "Spots merging into patches"),
                    List.of("Calcium nitrate", "Organic manure"),
                    List.of("Streptomycin sulfate", "Copper-based bactericide"),
                    List.of("Use disease-free seeds", "Rotate crops annually", "Sanitize farm tools")
            ),
            buildPrediction(
                    "Healthy Leaf",
                    91.2,
                    "No significant disease detected. Leaf appears healthy with normal coloration.",
                    List.of("Uniform green color", "No spots or lesions", "Normal leaf texture"),
                    List.of("Maintain regular NPK schedule", "Organic compost"),
                    List.of("Preventive neem spray (optional)"),
                    List.of("Continue balanced irrigation", "Monitor weekly", "Keep field weed-free")
            )
    );

    public DiseasePrediction detect(MultipartFile image) throws IOException {
        int index = 0;
        if (image != null && !image.isEmpty()) {
            byte[] bytes = image.getBytes();
            index = Math.abs(java.util.Arrays.hashCode(bytes)) % SAMPLE_PREDICTIONS.size();
        }
        DiseasePrediction source = SAMPLE_PREDICTIONS.get(index);
        DiseasePrediction prediction = new DiseasePrediction(
                source.getDiseaseName(),
                source.getConfidence(),
                source.getDescription(),
                source.getRecommendedFertilizers(),
                source.getRecommendedPesticides(),
                source.getPreventionTips()
        );
        prediction.setSymptoms(source.getSymptoms());
        prediction.setPlaceholder(true);
        return prediction;
    }

    private static DiseasePrediction buildPrediction(String name, double confidence, String description,
                                                     List<String> symptoms, List<String> fertilizers,
                                                     List<String> pesticides, List<String> tips) {
        DiseasePrediction p = new DiseasePrediction(name, confidence, description, fertilizers, pesticides, tips);
        p.setSymptoms(symptoms);
        return p;
    }
}
