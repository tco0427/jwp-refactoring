package kitchenpos.order.application;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kitchenpos.order.application.request.OrderLineItemRequest;
import kitchenpos.order.application.request.OrderRequest;
import kitchenpos.order.application.response.OrderResponse;
import kitchenpos.menu.domain.Menu;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.menu.domain.repository.MenuRepository;
import kitchenpos.order.domain.repository.OrderRepository;
import kitchenpos.table.domain.repository.OrderTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(
            final MenuRepository menuRepository,
            final OrderRepository orderRepository,
            final OrderTableRepository orderTableRepository
    ) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
    }

    public OrderResponse create(final OrderRequest request) {
        validateSavedMenuSize(request);
        validateOrderTableNotEmpty(request);

        final Order order = Order.of(request.getOrderTableId(), LocalDateTime.now(),
                getOrderLineItems(request));
        final Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        final List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(OrderResponse::from)
                .collect(toList());
    }

    public OrderResponse changeOrderStatus(final Long orderId, final OrderRequest request) {
        final Order savedOrder = orderRepository.findById(orderId)
                .orElseThrow(IllegalArgumentException::new);

        final OrderStatus orderStatus = OrderStatus.valueOf(request.getOrderStatus());
        savedOrder.changeOrderStatus(orderStatus);

        return OrderResponse.from(savedOrder);
    }

    private void validateSavedMenuSize(final OrderRequest request) {
        final List<Long> menuIds = request.getOrderLineItems()
                .stream()
                .map(OrderLineItemRequest::getMenuId)
                .collect(toList());

        if (request.getOrderLineItems().size() != menuRepository.countByIdIn(menuIds)) {
            throw new IllegalArgumentException("주문한 메뉴 항목 개수와 실제 메뉴의 수가 일치하지 않습니다.");
        }
    }

    private void validateOrderTableNotEmpty(final OrderRequest request) {
        final OrderTable orderTable = orderTableRepository.findById(request.getOrderTableId())
                .orElseThrow(IllegalArgumentException::new);

        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException("주문 테이블이 비어있으면 주문을 생성할 수 없다.");
        }
    }

    private List<OrderLineItem> getOrderLineItems(final OrderRequest request) {
        final List<OrderLineItemRequest> orderLineItemRequests = request.getOrderLineItems();
        final Map<Long, Long> menuAndQuantity = orderLineItemRequests.stream()
                .collect(toMap(OrderLineItemRequest::getMenuId, OrderLineItemRequest::getQuantity));

        final List<Menu> menus = menuRepository.findAllById(menuAndQuantity.keySet());

        return getOrderLineItems(menuAndQuantity, menus);
    }

    private static List<OrderLineItem> getOrderLineItems(final Map<Long, Long> menuAndQuantity, final List<Menu> menus) {
        final List<OrderLineItem> orderLineItems = new ArrayList<>();
        for (Menu menu : menus) {
            final OrderLineItem orderLineItem = new OrderLineItem(menu.getId(), menuAndQuantity.get(menu.getId()));
            orderLineItems.add(orderLineItem);
        }

        return orderLineItems;
    }
}
