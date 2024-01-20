package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.repositories.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingListServiceImpl implements ShoppingListService {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GenericProductRepository genericProductRepository;

    @Autowired
    ShoppingListRepository shoppingListRepository;

    @Autowired
    ProductQuantityGenericRepository productQuantityGenericRepository;

    @Autowired
    ProductQuantityFastestRepository productQuantityFastestRepository;

    @Autowired
    ProductQuantityCheapestRepository productQuantityCheapestRepository;

    @Autowired
    ChainRepository chainRepository;

    @Override
    public void addProduct(Long genericProductId) {

        User user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new BadRequestException("Utilizador não encontrado");
        }

        ShoppingList shoppingList = user.getCurrentShoppingList();
        GenericProduct genericProductToAdd = genericProductRepository.findById(genericProductId).get();

        // Se ainda não houver lista
        if (shoppingList == null) {

            shoppingList = new ShoppingList();
            shoppingList.setCreationDate(LocalDateTime.now());

            List<ProductQuantityGeneric> genericProductQuantities = new ArrayList<>();
            ProductQuantityGeneric productQuantityGeneric = new ProductQuantityGeneric();
            productQuantityGeneric.setQuantity(1);
            productQuantityGeneric.setGenericProduct(genericProductToAdd);
            productQuantityGeneric.setShoppingList(shoppingList);
            genericProductQuantities.add(productQuantityGeneric);
            shoppingList.setGenericProductQuantities(genericProductQuantities);

            List<ProductQuantityCheapest> cheapestProductQuantities = new ArrayList<>();
            ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
            productQuantityCheapest.setQuantity(1);
            productQuantityCheapest.setProduct(genericProductToAdd.getCurrentCheapestProduct());
            productQuantityCheapest.setShoppingList(shoppingList);
            cheapestProductQuantities.add(productQuantityCheapest);
            shoppingList.setCheapestProductQuantities(cheapestProductQuantities);

            List<ProductQuantityFastest> fastestProductQuantities = new ArrayList<>();
            ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
            productQuantityFastest.setQuantity(1);
            productQuantityFastest.setProduct(genericProductToAdd.getCurrentCheapestProduct());
            productQuantityFastest.setShoppingList(shoppingList);
            fastestProductQuantities.add(productQuantityFastest);
            shoppingList.setFastestProductQuantities(fastestProductQuantities);

            shoppingList.setCheapestListCost(genericProductToAdd.getCurrentLowestPricePrimaryValue());
            shoppingList.setFastestListCost(genericProductToAdd.getCurrentLowestPricePrimaryValue());

            shoppingList.setCurrentShoppingListForUser(user);
            user.setCurrentShoppingList(shoppingList);
            user.getShoppingLists().add(shoppingList);

            shoppingListRepository.save(shoppingList);
            productQuantityGenericRepository.save(productQuantityGeneric);
            productQuantityCheapestRepository.save(productQuantityCheapest);
            productQuantityFastestRepository.save(productQuantityFastest);
//            userRepository.save(user);
            return;
        }

        // Adicionar o produto à lista genérica
        List<ProductQuantityGeneric> genericProductQuantities = new ArrayList<>(productQuantityGenericRepository.findAllByShoppingList(shoppingList));
        int genericProductQuantitiesSize = genericProductQuantities.size();

        for (int i = 0; i < genericProductQuantitiesSize; i++) {
            ProductQuantityGeneric genericProductQuantity = genericProductQuantities.get(i);
            if (genericProductQuantity.getGenericProduct().getId().equals(genericProductId)) {
                // Se o produto já existe na lista, é só preciso atualizar a quantidade e os custos
                genericProductQuantity.setQuantity(genericProductQuantity.getQuantity() + 1);
                shoppingListRepository.save(shoppingList);
                productQuantityGenericRepository.save(genericProductQuantity);
                break;
            } else if (i == genericProductQuantitiesSize - 1) {
                // Se não existe na lista, adicionar
                ProductQuantityGeneric productQuantityGeneric = new ProductQuantityGeneric();
                productQuantityGeneric.setGenericProduct(genericProductToAdd);
                productQuantityGeneric.setQuantity(1);
                productQuantityGeneric.setShoppingList(shoppingList);
                genericProductQuantities.add(productQuantityGeneric);
//                shoppingList.setGenericProductQuantities(genericProductQuantities);
                shoppingListRepository.save(shoppingList);
                productQuantityGenericRepository.save(productQuantityGeneric);
                break;
            }
        }

        /////////////////////// Gerar listas mais rápida e mais barata /////////////////////////////////

        productQuantityCheapestRepository.deleteInBatch(shoppingList.getCheapestProductQuantities());
        productQuantityFastestRepository.deleteInBatch(shoppingList.getFastestProductQuantities());
        shoppingList.setFastestProductQuantities(new ArrayList<>());
        shoppingList.setCheapestProductQuantities(new ArrayList<>());
        shoppingListRepository.save(shoppingList);

        // Se só houver um produto na lista, a lista mais rápida e mais barata são iguais
        if (genericProductQuantities.size() == 1) {

            ProductQuantityGeneric genericProductQuantity = genericProductQuantities.get(0);
            GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
            Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
            double listCost = genericProductQuantity.getQuantity() * genericProduct.getCurrentLowestPricePrimaryValue();

            ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
            productQuantityFastest.setProduct(currentCheapestProduct);
            productQuantityFastest.setShoppingList(shoppingList);
            productQuantityFastest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getFastestProductQuantities().add(productQuantityFastest);
            shoppingList.setFastestListCost(listCost);
//            productQuantityFastestRepository.save(productQuantityFastest);

            ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
            productQuantityCheapest.setProduct(currentCheapestProduct);
            productQuantityCheapest.setShoppingList(shoppingList);
            productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getCheapestProductQuantities().add(productQuantityCheapest);
            shoppingList.setCheapestListCost(listCost);
//            productQuantityCheapestRepository.save(productQuantityCheapest);

            shoppingListRepository.save(shoppingList);
//            productQuantityCheapestRepository.save(productQuantityCheapest);
//            productQuantityFastestRepository.save(productQuantityFastest);

        } else {

            ////////////////////// Criar lista de produtos "mais rápida" //////////////////////////////////
            List<Chain> chains = new ArrayList<>(chainRepository.findAll());

            // Inicializar lista para contagem de superfícies
            int[] countProductChains = new int[chains.size()];

            // Obter lista com todos os produtos
            List<Product> productsInList = genericProductQuantities.stream()
                    .flatMap(productQuantityGeneric -> productQuantityGeneric.getGenericProduct().getProducts().stream())
                    .collect(Collectors.toList());

            // Contar superfícies
            for (Product product : productsInList) {
                Chain productChain = product.getChain();
                int index = chains.indexOf(productChain);
                countProductChains[index] += 1;
            }

            // Criar lista com superfícies e contagem
            List<Map<String, Object>> countProductChainsList = new ArrayList<>();


            for (int i = 0; i < countProductChains.length; i++) {
                Map<String, Object> chainCounter = new HashMap<>();
                chainCounter.put("chain", chains.get(i));
                chainCounter.put("count", countProductChains[i]);
                countProductChainsList.add(chainCounter);
            }

            // Ordenar lista
            Collections.sort(countProductChainsList, Comparator.comparingInt(map -> (Integer) map.get("count")));

            // Criar lista "mais rápida"
            List<ProductQuantityFastest> productQuantityFastestList = new ArrayList<>();
            double fastestListCost = 0;

            while (productQuantityFastestList.size() < genericProductQuantities.size()) {
                for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
                    List<Product> products = genericProductQuantity.getGenericProduct().getProducts();
                    boolean moveToNextGenericProduct = false;
                    for (int i = countProductChainsList.size() - 1; i >= 0; i--) {
                        if (moveToNextGenericProduct) {
                            break;
                        }
                        for (Product product : products) {
                            if (product.getChain().getName().equals(((Chain) countProductChainsList.get(i).get("chain")).getName())) {
                                int quantity = genericProductQuantity.getQuantity();
                                Price currentPrice = product.getPrices().get(product.getPrices().size() - 1);
                                ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
                                productQuantityFastest.setProduct(product);
                                productQuantityFastest.setShoppingList(shoppingList);
                                productQuantityFastest.setQuantity(quantity);
                                productQuantityFastestList.add(productQuantityFastest);
                                fastestListCost += quantity * currentPrice.getPrimaryValue();
                                moveToNextGenericProduct = true;
                                break;
                            }
                        }
                    }
                }
            }

            shoppingList.setFastestListCost(fastestListCost);
            shoppingListRepository.save(shoppingList);
            productQuantityFastestRepository.saveAll(productQuantityFastestList);

            ////////////////////// Criar lista de produtos "mais barata" //////////////////////////////////

            List<ProductQuantityCheapest> productQuantityCheapestList = new ArrayList<>();
            double cheapestListCost = 0;

            for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
                int quantity = genericProductQuantity.getQuantity();
                GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
                Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
                ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
                productQuantityCheapest.setProduct(currentCheapestProduct);
                productQuantityCheapest.setShoppingList(shoppingList);
                productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
                productQuantityCheapestList.add(productQuantityCheapest);
                cheapestListCost += quantity * genericProduct.getCurrentLowestPricePrimaryValue();
            }

            shoppingList.setCheapestListCost(cheapestListCost);
            shoppingListRepository.save(shoppingList);
            productQuantityCheapestRepository.saveAll(productQuantityCheapestList);
        }

        shoppingListRepository.save(shoppingList);
//        userRepository.save(user);
    }

    @Override
    public void removeProduct(Long genericProductId) {

        User user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new BadRequestException("Utilizador não encontrado");
        }

        ShoppingList shoppingList = user.getCurrentShoppingList();
        List<ProductQuantityGeneric> genericProductQuantities = shoppingList.getGenericProductQuantities();
        ProductQuantityGeneric productQuantityGenericToUpdate = null;
        for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
            if (genericProductQuantity.getGenericProduct().getId().equals(genericProductId)) {
                productQuantityGenericToUpdate = genericProductQuantity;
                break;
            }
        }

        int quantity = productQuantityGenericToUpdate.getQuantity();
        // Caso a quantidade seja maior que 1, basta atualizar as quantidades e custos
        if (quantity > 1) {
            int updatedQuantity = quantity - 1;
            productQuantityGenericToUpdate.setQuantity(updatedQuantity);
            boolean fastestListUpdated = false;
            boolean cheapestListUpdated = false;
            for (int i = 0; i < genericProductQuantities.size(); i++) {
                ProductQuantityFastest productQuantityFastest = shoppingList.getFastestProductQuantities().get(i);
                Product productInFastestList = productQuantityFastest.getProduct();
                if (productInFastestList.getGenericProduct().getId().equals(genericProductId)) {
                    productQuantityFastest.setQuantity(updatedQuantity);
                    List<Price> productInFastestListPrices = productInFastestList.getPrices();
                    shoppingList.setFastestListCost(shoppingList.getFastestListCost() - productInFastestListPrices.get(productInFastestListPrices.size() - 1).getPrimaryValue());
                    fastestListUpdated = true;
                }
                ProductQuantityCheapest productQuantityCheapest = shoppingList.getCheapestProductQuantities().get(i);
                Product productInCheapestList = productQuantityCheapest.getProduct();
                if (productInCheapestList.getGenericProduct().getId().equals(genericProductId)) {
                    productQuantityCheapest.setQuantity(updatedQuantity);
                    List<Price> productInCheapestListPrices = productInFastestList.getPrices();
                    shoppingList.setCheapestListCost(shoppingList.getCheapestListCost() - productInCheapestListPrices.get(productInCheapestListPrices.size() - 1).getPrimaryValue());
                    cheapestListUpdated = true;
                }
                if (fastestListUpdated && cheapestListUpdated)
                    break;
            }
        } else {
            //Caso contrário, remover o produto das listas e re-calcular listas mais rápida e mais barata e custos
            genericProductQuantities.remove(productQuantityGenericToUpdate);
            List<ProductQuantityGeneric> updatedGenericList = new ArrayList<>(genericProductQuantities);
            shoppingList.setGenericProductQuantities(updatedGenericList);
            productQuantityGenericRepository.delete(productQuantityGenericToUpdate);
//            shoppingListRepository.save(shoppingList);
            this.generateLists(shoppingList);
            return;
        }

        shoppingListRepository.save(shoppingList);

    }

    @Override
    public ShoppingList getCurrentShoppingList() {
        User user = userService.getAuthenticatedUser();
        if (user == null) throw new BadRequestException("Utilizador não encontrado");
        ShoppingList shoppingList = user.getCurrentShoppingList();
        if (shoppingList == null) throw new BadRequestException("Lista não encontrada");
        return shoppingList;
    }

    private void updateQuantityAndCosts(ShoppingList shoppingList, ProductQuantityGeneric productQuantityGenericToUpdate, Long genericProductId, boolean add){

        List<ProductQuantityGeneric> genericProductQuantities = shoppingList.getGenericProductQuantities();
        int quantity = productQuantityGenericToUpdate.getQuantity();
        int updatedQuantity = quantity;
        updatedQuantity = add ? updatedQuantity + 1 : updatedQuantity - 1;
        productQuantityGenericToUpdate.setQuantity(updatedQuantity);
        boolean fastestListUpdated = false;
        boolean cheapestListUpdated = false;
        for (int i = 0; i < genericProductQuantities.size(); i++) {
            ProductQuantityFastest productQuantityFastest = shoppingList.getFastestProductQuantities().get(i);
            Product productInFastestList = productQuantityFastest.getProduct();
            if (productInFastestList.getGenericProduct().getId().equals(genericProductId)) {
                productQuantityFastest.setQuantity(updatedQuantity);
                List<Price> productInFastestListPrices = productInFastestList.getPrices();
                double currentFastestCost = shoppingList.getFastestListCost();
                double productInFastestListPrice = productInFastestListPrices.get(productInFastestListPrices.size() - 1).getPrimaryValue();
                double updatedFastestListCost = add ?  currentFastestCost + productInFastestListPrice : currentFastestCost - productInFastestListPrice;
                shoppingList.setFastestListCost(updatedFastestListCost);
                fastestListUpdated = true;
            }
            ProductQuantityCheapest productQuantityCheapest = shoppingList.getCheapestProductQuantities().get(i);
            Product productInCheapestList = productQuantityCheapest.getProduct();
            if (productInCheapestList.getGenericProduct().getId().equals(genericProductId)) {
                productQuantityCheapest.setQuantity(updatedQuantity);
                List<Price> productInCheapestListPrices = productInFastestList.getPrices();
                double currentCheapestCost = shoppingList.getCheapestListCost();
                double productInCheapestListPrice = productInCheapestListPrices.get(productInCheapestListPrices.size() - 1).getPrimaryValue();
                double updatedCheapestListCost = add ?  currentCheapestCost + productInCheapestListPrice : currentCheapestCost - productInCheapestListPrice;
                shoppingList.setCheapestListCost(updatedCheapestListCost);
                cheapestListUpdated = true;
            }
            if (fastestListUpdated && cheapestListUpdated)
                break;
        }
    }

    private void generateLists(ShoppingList shoppingList){

        List<ProductQuantityGeneric> genericProductQuantities = shoppingList.getGenericProductQuantities();

        /////////////////////// Gerar listas mais rápida e mais barata /////////////////////////////////

        productQuantityCheapestRepository.deleteInBatch(shoppingList.getCheapestProductQuantities());
        productQuantityFastestRepository.deleteInBatch(shoppingList.getFastestProductQuantities());
        shoppingList.setFastestProductQuantities(new ArrayList<>());
        shoppingList.setCheapestProductQuantities(new ArrayList<>());
//        shoppingListRepository.save(shoppingList);

        // Se só houver um produto na lista, a lista mais rápida e mais barata são iguais
        if (genericProductQuantities.size() == 1) {

            ProductQuantityGeneric genericProductQuantity = genericProductQuantities.get(0);
            GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
            Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
            double listCost = genericProductQuantity.getQuantity() * genericProduct.getCurrentLowestPricePrimaryValue();

            ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
            productQuantityFastest.setProduct(currentCheapestProduct);
            productQuantityFastest.setShoppingList(shoppingList);
            productQuantityFastest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getFastestProductQuantities().add(productQuantityFastest);
            shoppingList.setFastestListCost(listCost);

            ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
            productQuantityCheapest.setProduct(currentCheapestProduct);
            productQuantityCheapest.setShoppingList(shoppingList);
            productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getCheapestProductQuantities().add(productQuantityCheapest);
            shoppingList.setCheapestListCost(listCost);

//            shoppingListRepository.save(shoppingList);

        } else {

            ////////////////////// Criar lista de produtos "mais rápida" //////////////////////////////////
            List<Chain> chains = new ArrayList<>(chainRepository.findAll());

            // Inicializar lista para contagem de superfícies
            int[] countProductChains = new int[chains.size()];

            // Obter lista com todos os produtos
            List<Product> productsInList = genericProductQuantities.stream()
                    .flatMap(productQuantityGeneric -> productQuantityGeneric.getGenericProduct().getProducts().stream())
                    .collect(Collectors.toList());

            // Contar superfícies
            for (Product product : productsInList) {
                Chain productChain = product.getChain();
                int index = chains.indexOf(productChain);
                countProductChains[index] += 1;
            }

            // Criar lista com superfícies e contagem
            List<Map<String, Object>> countProductChainsList = new ArrayList<>();


            for (int i = 0; i < countProductChains.length; i++) {
                Map<String, Object> chainCounter = new HashMap<>();
                chainCounter.put("chain", chains.get(i));
                chainCounter.put("count", countProductChains[i]);
                countProductChainsList.add(chainCounter);
            }

            // Ordenar lista
            Collections.sort(countProductChainsList, Comparator.comparingInt(map -> (Integer) map.get("count")));

            // Criar lista "mais rápida"
            List<ProductQuantityFastest> productQuantityFastestList = new ArrayList<>();
            double fastestListCost = 0;

            while (productQuantityFastestList.size() < genericProductQuantities.size()) {
                for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
                    List<Product> products = genericProductQuantity.getGenericProduct().getProducts();
                    boolean moveToNextGenericProduct = false;
                    for (int i = countProductChainsList.size() - 1; i >= 0; i--) {
                        if (moveToNextGenericProduct) {
                            break;
                        }
                        for (Product product : products) {
                            if (product.getChain().getName().equals(((Chain) countProductChainsList.get(i).get("chain")).getName())) {
                                int quantity = genericProductQuantity.getQuantity();
                                Price currentPrice = product.getPrices().get(product.getPrices().size() - 1);
                                ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
                                productQuantityFastest.setProduct(product);
                                productQuantityFastest.setShoppingList(shoppingList);
                                productQuantityFastest.setQuantity(quantity);
                                productQuantityFastestList.add(productQuantityFastest);
                                fastestListCost += quantity * currentPrice.getPrimaryValue();
                                moveToNextGenericProduct = true;
                                break;
                            }
                        }
                    }
                }
            }

            shoppingList.setFastestListCost(fastestListCost);
//            shoppingListRepository.save(shoppingList);
            productQuantityFastestRepository.saveAll(productQuantityFastestList);

            ////////////////////// Criar lista de produtos "mais barata" //////////////////////////////////

            List<ProductQuantityCheapest> productQuantityCheapestList = new ArrayList<>();
            double cheapestListCost = 0;

            for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
                int quantity = genericProductQuantity.getQuantity();
                GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
                Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
                ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
                productQuantityCheapest.setProduct(currentCheapestProduct);
                productQuantityCheapest.setShoppingList(shoppingList);
                productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
                productQuantityCheapestList.add(productQuantityCheapest);
                cheapestListCost += quantity * genericProduct.getCurrentLowestPricePrimaryValue();
            }

            shoppingList.setCheapestListCost(cheapestListCost);
//            shoppingListRepository.save(shoppingList);
            productQuantityCheapestRepository.saveAll(productQuantityCheapestList);
        }

        shoppingListRepository.save(shoppingList);
    }


}