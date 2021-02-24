package com.ebebek.reactiveredis.service;

import com.ebebek.reactiveredis.model.cart.Cart;
import com.ebebek.reactiveredis.model.cart.CartItem;
import com.ebebek.reactiveredis.model.cart.CartRequest;
import com.ebebek.reactiveredis.model.cart.CartResponse;
import com.ebebek.reactiveredis.model.product.ProductRequest;
import com.ebebek.reactiveredis.util.ResponseCodesUtil;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartService extends BaseService {

    private static final String CARTS_KEY = "Carts";

    private HashOperations<String, String, Cart> hashOperations;

    public CartService(RedisTemplate redisTemplate) {
        redisTemplate.setHashValueSerializer(serializerSetup());
        hashOperations = redisTemplate.opsForHash();
    }

    public CartResponse createCart(@RequestBody CartRequest request) {
        Cart cart = request.getCart();
        cart.setId(UUID.randomUUID().toString());
        Map<String, Cart> cartMap = Stream.of(
                new AbstractMap.SimpleEntry<>(cart.getId(), cart)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        hashOperations.putAll(CARTS_KEY, cartMap);
        return new CartResponse(Collections.singletonList(cart), ResponseCodesUtil.SUCCESS.message, ResponseCodesUtil.SUCCESS.code);
    }

    public List<Cart> getAllCarts() {
        return hashOperations.values(CARTS_KEY);
    }

    public Cart getCartById(@RequestParam("id") String id) {
        return hashOperations.get(CARTS_KEY, id);
    }

    public CartResponse modifyCart(@RequestBody CartRequest request) {
        hashOperations.put(CARTS_KEY, request.getCart().getId(), request.getCart());
        return new CartResponse(Collections.singletonList(request.getCart()), ResponseCodesUtil.SUCCESS.message, ResponseCodesUtil.SUCCESS.code);
    }

    public CartResponse incrementProductCount(@RequestBody ProductRequest request) {
        return changeProductCount(request, IncDec.INC);
    }

    public CartResponse decrementProductCount(@RequestBody ProductRequest request) {
        return changeProductCount(request, IncDec.DEC);
    }

    private CartResponse changeProductCount(ProductRequest request, IncDec incDec) {
        Cart cart = hashOperations.get(CARTS_KEY, request.getCartId());

        switch(incDec) {
            case INC :
                cart.getCartItemList().stream()
                        .filter(e -> e.getProduct().getId().equals(request.getProductId()))
                        .findFirst().orElse(new CartItem())
                        .increment();
                break;
            case DEC :
                cart.getCartItemList().stream()
                        .filter(e -> e.getProduct().getId().equals(request.getProductId()))
                        .findFirst().orElse(new CartItem())
                        .decrement();
                break;
        }

        cart.refresh();
        redisTemplate.opsForHash().put(CARTS_KEY, cart.getId(), cart);
        return new CartResponse(Collections.singletonList(cart), ResponseCodesUtil.SUCCESS.message, ResponseCodesUtil.SUCCESS.code);
    }

    enum IncDec {
        INC,
        DEC,
        INCx10,
        DECx10;
    }
}
