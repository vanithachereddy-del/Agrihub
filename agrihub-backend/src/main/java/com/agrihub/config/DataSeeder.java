package com.agrihub.config;

import com.agrihub.model.CropGuide;
import com.agrihub.model.Farmer;
import com.agrihub.model.Product;
import com.agrihub.model.User;
import com.agrihub.repository.CropGuideRepository;
import com.agrihub.repository.FarmerRepository;
import com.agrihub.repository.ProductRepository;
import com.agrihub.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds initial demo data when collections are empty.
 */
@Component
public class DataSeeder implements ApplicationRunner {

        private final ProductRepository productRepository;
        private final CropGuideRepository cropGuideRepository;
        private final UserRepository userRepository;
        private final FarmerRepository farmerRepository;

        public DataSeeder(ProductRepository productRepository,
                        CropGuideRepository cropGuideRepository,
                        UserRepository userRepository,
                        FarmerRepository farmerRepository) {
                this.productRepository = productRepository;
                this.cropGuideRepository = cropGuideRepository;
                this.userRepository = userRepository;
                this.farmerRepository = farmerRepository;
        }

        @Override
        public void run(@Nullable ApplicationArguments args) {
                seedProducts();
                seedCrops();
                seedAdmin();
                seedFarmers();
        }

        private void seedProducts() {
        productRepository.deleteAll();
        List<Product> products = new ArrayList<>();
        
        // 1. Fertilizers
        products.add(product("Urea", "IFFCO", "Fertilizers", 1300.0, 120, "bag", "45 Kg Bag",
                "High nitrogen fertilizer for vegetative growth in cereals and vegetables.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("DAP", "Coromandel", "Fertilizers", 1450.0, 85, "bag", "50 Kg Bag",
                "Diammonium phosphate for root development and flowering.",
                "https://images.unsplash.com/photo-1530836369250-ef72a3f5cda8?w=400"));
        products.add(product("MOP (Potash)", "IPL", "Fertilizers", 1200.0, 60, "bag", "50 Kg Bag",
                "Muriate of Potash to improve disease resistance and crop quality.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("SSP", "Coromandel", "Fertilizers", 550.0, 90, "bag", "50 Kg Bag",
                "Single Super Phosphate supplying phosphorus, sulphur, and calcium.",
                "https://images.unsplash.com/photo-1530836369250-ef72a3f5cda8?w=400"));
        products.add(product("NPK 20:20:0", "Nagarjuna", "Fertilizers", 1450.0, 50, "bag", "50 Kg Bag",
                "Nitrogen and Phosphate compound fertilizer.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("NPK 19:19:19", "IFFCO", "Fertilizers", 1650.0, 45, "bag", "25 Kg Bag",
                "Balanced water-soluble NPK for all crop stages.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("NPK 10:26:26", "Zuari", "Fertilizers", 1700.0, 40, "bag", "50 Kg Bag",
                "High potassium compound fertilizer for potato, onion, and fruits.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Zinc Sulphate", "Aries", "Fertilizers", 750.0, 30, "bag", "10 Kg Bag",
                "Essential micronutrient to cure zinc deficiency.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Boron", "Aries", "Fertilizers", 450.0, 100, "pack", "1 Kg",
                "Micronutrient for fruit setting and crop quality.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Gypsum", "Local", "Fertilizers", 350.0, 150, "bag", "40 Kg Bag",
                "Soil conditioner supplying calcium and sulphur.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));

        // 2. Pesticides
        products.add(product("Confidor", "Bayer", "Pesticides", 450.0, 80, "bottle", "250 ml",
                "Imidacloprid formulation for control of sucking pests in various crops.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Monocrotophos", "UPL", "Pesticides", 600.0, 40, "bottle", "500 ml",
                "Organophosphate insecticide to control stem borers and leaf eating pests.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Chlorpyrifos", "Dow", "Pesticides", 500.0, 50, "bottle", "1 Litre",
                "Broad spectrum insecticide for control of soil insects and termites.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Imidacloprid", "Tata", "Pesticides", 350.0, 75, "bottle", "250 ml",
                "Systemic insecticide targeting aphids, whiteflies, and jassids.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Acephate", "Multiplex", "Pesticides", 450.0, 55, "pack", "500 g",
                "Soluble powder insecticide for caterpillar and leaf folder control.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Thiamethoxam", "Syngenta", "Pesticides", 400.0, 65, "pack", "250 g",
                "Broad-spectrum systemic insecticide for sucking pests.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Fipronil", "BASF", "Pesticides", 750.0, 30, "bottle", "500 ml",
                "Insecticide for root borer, stem borer, and thrips control.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Profenofos", "Syngenta", "Pesticides", 650.0, 35, "bottle", "500 ml",
                "Insecticide and acaricide targeting bollworm and armyworm.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Lambda Cyhalothrin", "Syngenta", "Pesticides", 480.0, 40, "bottle", "500 ml",
                "Synthetic pyrethroid for rapid knockdown of insect pests.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Cypermethrin", "UPL", "Pesticides", 380.0, 60, "bottle", "1 Litre",
                "General purpose insecticide for crop security.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));

        // 3. Fungicides
        products.add(product("Mancozeb", "Indofil", "Fungicides", 320.0, 70, "pack", "500 g",
                "Contact fungicide for leaf spot, blight, and downy mildew control.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Carbendazim", "BASF", "Fungicides", 280.0, 60, "pack", "250 g",
                "Systemic fungicide for collar rot and wilt disease treatment.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Copper Oxychloride", "Tata", "Fungicides", 450.0, 40, "pack", "500 g",
                "Contact copper fungicide offering multi-site activity against fungal diseases.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Hexaconazole", "Rallis", "Fungicides", 380.0, 45, "bottle", "500 ml",
                "Systemic fungicide targeting powdery mildew and scab.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Propiconazole", "Syngenta", "Fungicides", 550.0, 30, "bottle", "500 ml",
                "Triazole fungicide for rust and leaf spot management.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Metalaxyl", "Bayer", "Fungicides", 620.0, 25, "pack", "250 g",
                "Systemic fungicide for downy mildew and damp-off control.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Tricyclazole", "Dow", "Fungicides", 850.0, 35, "pack", "500 g",
                "Specialized systemic fungicide for Rice Blast control.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Azoxystrobin", "Syngenta", "Fungicides", 1250.0, 20, "bottle", "200 ml",
                "Broad spectrum systemic fungicide for premium crop disease protection.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));

        // 4. Herbicides
        products.add(product("Glyphosate", "Monsanto", "Herbicides", 850.0, 50, "bottle", "1 Litre",
                "Non-selective systemic herbicide to eliminate all weeds.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Pendimethalin", "BASF", "Herbicides", 680.0, 45, "bottle", "1 Litre",
                "Pre-emergence herbicide to control grass weeds in early crop stages.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Butachlor", "UPL", "Herbicides", 450.0, 50, "bottle", "1 Litre",
                "Pre-emergence selective herbicide specifically for Rice fields.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Atrazine", "Tata", "Herbicides", 380.0, 60, "pack", "500 g",
                "Selective herbicide for control of weeds in Maize crops.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Paraquat", "Syngenta", "Herbicides", 550.0, 40, "bottle", "1 Litre",
                "Fast acting contact herbicide.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));
        products.add(product("Oxyfluorfen", "Dow", "Herbicides", 620.0, 30, "bottle", "250 ml",
                "Selective herbicide for control of broadleaf weeds.",
                "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400"));

        // 5. Bio Fertilizers
        products.add(product("Azospirillum", "Multiplex", "Bio Fertilizers", 220.0, 100, "bottle", "1 Litre",
                "Nitrogen fixing bio fertilizer for cereals and cash crops.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Rhizobium", "Multiplex", "Bio Fertilizers", 200.0, 100, "bottle", "1 Litre",
                "Legume-specific nitrogen fixing bio fertilizer.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Azotobacter", "Multiplex", "Bio Fertilizers", 210.0, 100, "bottle", "1 Litre",
                "Free-living nitrogen fixing bio fertilizer.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Phosphate Solubilizing Bacteria", "Multiplex", "Bio Fertilizers", 230.0, 100, "bottle", "1 Litre",
                "Solubilizes insoluble phosphate in the soil for easy uptake.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
        products.add(product("Trichoderma", "Multiplex", "Bio Fertilizers", 180.0, 120, "pack", "1 Kg",
                "Bio-fungicide to prevent root rot and other soil borne diseases.",
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));

        productRepository.saveAll(products);
    }

    private Product product(String name, String brand, String category, double price, int stock,
                            String unit, String quantity, String description, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(category);
        p.setPrice(price);
        p.setStock(stock);
        p.setUnit(unit);
        p.setQuantity(quantity);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        return p;
    }

    private void seedCrops() {
        cropGuideRepository.deleteAll();
        List<CropGuide> crops = new ArrayList<>();
        
        crops.add(crop("Rice", "Cereal",
                "Rice is a staple crop that grows well in flooded soil and warm climates.",
                "1. Seedling 2. Tillering 3. Panicle initiation 4. Flowering 5. Harvest",
                "Apply Urea, DAP, Potash and Zinc Sulphate at appropriate stages.",
                "Maintain 2–5 cm water depth during vegetative growth (High requirement).",
                "120–150 Days", "Clay soil and Loamy soil", "Kharif",
                Arrays.asList("Blast", "Brown Spot", "Sheath Blight", "Stem Borer", "Brown Plant Hopper"),
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400"));
                
        crops.add(crop("Wheat", "Cereal",
                "Wheat is a key food grain grown in cool dry seasons across India.",
                "1. Germination 2. Tillering 3. Jointing 4. Heading 5. Maturity",
                "Basal DAP at sowing; top dress Urea during crown root initiation.",
                "Requires 4–6 timely irrigations at critical stages.",
                "110–130 Days", "Loam and clay loam soils", "Rabi",
                Arrays.asList("Rust", "Karnal bunt", "Loose smut", "Aphids"),
                "https://images.unsplash.com/photo-1530836369250-ef72a3f5cda8?w=400"));
                
        crops.add(crop("Cotton", "Cash Crop",
                "Cotton requires long frost-free period and moderate rainfall.",
                "1. Sowing 2. Squaring 3. Flowering 4. Boll formation 5. Picking",
                "Apply DAP, Urea, and Zinc as per soil testing.",
                "Critical irrigation at squaring and boll formation stages (Medium requirement).",
                "160–180 Days", "Black soil", "Kharif",
                Arrays.asList("Wilt", "Leaf Curl Virus", "Pink Bollworm", "Whitefly"),
                "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400"));
                
        crops.add(crop("Tomato", "Vegetable",
                "Tomatoes need well-drained soil, bright sunlight and regular feeding.",
                "1. Germination 2. Vegetative 3. Flowering 4. Fruiting 5. Harvest",
                "Compost at planting; NPK and Urea during flowering.",
                "Deep watering 2–3 times per week depending on weather (Moderate requirement).",
                "90–120 Days", "Sandy Loam", "All Seasons",
                Arrays.asList("Early Blight", "Late Blight", "Fruit Borer", "Whitefly"),
                "https://images.unsplash.com/photo-1592419044706-39796d40f98c?w=400"));

        crops.add(crop("Chilli", "Vegetable",
                "Chilli grows well in warm, humid climates and is a major commercial spice.",
                "1. Germination 2. Transplanting 3. Flowering 4. Fruit set 5. Harvesting",
                "NPK during growth, Hexaconazole for disease, and Imidacloprid for pests.",
                "Requires regular watering, avoid waterlogging.",
                "150 Days", "Well-drained loamy soil", "Kharif",
                Arrays.asList("Powdery Mildew", "Dieback", "Thrips", "Aphids"),
                "https://images.unsplash.com/photo-1592419044706-39796d40f98c?w=400"));

        crops.add(crop("Maize", "Cereal",
                "Maize is a versatile crop adaptable to diverse agro-climatic conditions.",
                "1. Sowing 2. V3-V5 Leaf stages 3. Tasseling 4. Silking 5. Maturity",
                "Apply Urea, DAP, Zinc Sulphate and Atrazine selective herbicide.",
                "Critical water needs during tasseling and grain filling.",
                "90–120 Days", "Well-drained sandy loam", "Kharif/Rabi",
                Arrays.asList("Downy Mildew", "Rust", "Stem Borer"),
                "https://images.unsplash.com/photo-1530836369250-ef72a3f5cda8?w=400"));

        applyRecommendationData(crops);
        cropGuideRepository.saveAll(crops);
    }


    /**
     * Adds the structured soil/season/water/pH fields used by the rule-based
     * Crop Recommendation engine (Phase 1). The values below are general,
     * commonly-cited agronomy reference ranges - they are NOT taken from a
     * specific validated dataset and should be cross-checked against
     * agricultural research or expert sources before being relied on for
     * real farming decisions.
     */
    private void applyRecommendationData(List<CropGuide> crops) {
        for (CropGuide c : crops) {
            String name = c.getName();
            if ("Rice".equals(name)) {
                c.setSoilTypes(Arrays.asList("Clay", "Loamy"));
                c.setSeasons(Arrays.asList("Kharif"));
                c.setWaterRequirement("High");
                c.setPhMin(5.5);
                c.setPhMax(6.5);
                c.setIrrigationSources(Arrays.asList("Canal", "Borewell", "Tank", "Rainfed"));
            } else if ("Wheat".equals(name)) {
                c.setSoilTypes(Arrays.asList("Loam", "Clay Loam"));
                c.setSeasons(Arrays.asList("Rabi"));
                c.setWaterRequirement("Medium");
                c.setPhMin(6.0);
                c.setPhMax(7.5);
                c.setIrrigationSources(Arrays.asList("Canal", "Borewell", "Drip", "Tank"));
            } else if ("Cotton".equals(name)) {
                c.setSoilTypes(Arrays.asList("Black soil"));
                c.setSeasons(Arrays.asList("Kharif"));
                c.setWaterRequirement("Medium");
                c.setPhMin(6.0);
                c.setPhMax(8.0);
                c.setIrrigationSources(Arrays.asList("Rainfed", "Canal", "Borewell", "Drip"));
            } else if ("Tomato".equals(name)) {
                c.setSoilTypes(Arrays.asList("Sandy Loam"));
                c.setSeasons(Arrays.asList("Kharif", "Rabi", "Zaid"));
                c.setWaterRequirement("Medium");
                c.setPhMin(6.0);
                c.setPhMax(6.8);
                c.setIrrigationSources(Arrays.asList("Drip", "Borewell", "Canal", "Tank"));
            } else if ("Chilli".equals(name)) {
                c.setSoilTypes(Arrays.asList("Loamy"));
                c.setSeasons(Arrays.asList("Kharif"));
                c.setWaterRequirement("Medium");
                c.setPhMin(6.0);
                c.setPhMax(7.0);
                c.setIrrigationSources(Arrays.asList("Drip", "Borewell", "Canal", "Tank", "Rainfed"));
            } else if ("Maize".equals(name)) {
                c.setSoilTypes(Arrays.asList("Sandy Loam"));
                c.setSeasons(Arrays.asList("Kharif", "Rabi"));
                c.setWaterRequirement("Medium");
                c.setPhMin(5.5);
                c.setPhMax(7.5);
                c.setIrrigationSources(Arrays.asList("Rainfed", "Canal", "Borewell", "Drip", "Tank"));
            }
        }
    }

    private CropGuide crop(String name, String category, String description, String growthStages,
                           String fertilizerSchedule, String irrigationSchedule, String harvestDuration,
                           String suitableSoil, String bestSeason, List<String> diseases, String imageUrl) {
        return new CropGuide(name, category, description, growthStages, fertilizerSchedule,
                irrigationSchedule, harvestDuration, suitableSoil, bestSeason, diseases, imageUrl);
        }

        private void seedAdmin() {
                if (userRepository.count() > 0) {
                        return;
                }
                User admin = new User("Shop Owner", "admin@agrihub.com", "admin123", "admin");
                userRepository.save(admin);
        }

        private void seedFarmers() {
                if (farmerRepository.count() > 0) {
                        return;
                }
                Farmer farmer = new Farmer("Ramesh Kumar", "ramesh@farmer.com", "9876543210", "farmer123",
                                "Near Main Road", "Green Valley Farm");
                farmer.setVillage("Rampur");
                farmer.setFarmSize("5 acres");
                farmer.setCrops(Arrays.asList("Rice", "Tomato"));
                farmerRepository.save(farmer);
        }
}
