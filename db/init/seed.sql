-- E-Commerce Sample Data

DO $$
DECLARE
    i INTEGER;
    j INTEGER;
    customer_uuid UUID;
    order_uuid UUID;
    status_options TEXT[] := ARRAY['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
    item_count INTEGER;
    item_price DECIMAL(12,2);
    item_quantity INTEGER;
    order_total DECIMAL(12,2);
    existing_count INTEGER;
BEGIN
    -- Check if data already exists
    SELECT COUNT(*) INTO existing_count FROM customers;

    IF existing_count > 0 THEN
        RAISE NOTICE 'Data already exists (% customers found), skipping seed', existing_count;
        RETURN;
    END IF;

    RAISE NOTICE 'Seeding database with sample data...';

    -- Insert 100 customers
    FOR i IN 1..100 LOOP
        INSERT INTO customers (name, email, address, created_at)
        VALUES (
            'Customer ' || i,
            'customer' || i || '@example.com',
            i || ' Main Street, City ' || (i % 10),
            CURRENT_TIMESTAMP - (random() * INTERVAL '365 days')
        );
    END LOOP;

    RAISE NOTICE 'Inserted 100 customers';

    -- Insert 500 orders with items
    FOR i IN 1..500 LOOP
        -- Get random customer
        SELECT id INTO customer_uuid FROM customers ORDER BY random() LIMIT 1;

        -- Calculate order details
        item_count := 1 + floor(random() * 5)::INTEGER; -- 1-5 items per order
        order_total := 0;

        -- Insert order with placeholder total
        INSERT INTO orders (customer_id, order_date, total_amount, status)
        VALUES (
            customer_uuid,
            CURRENT_TIMESTAMP - (random() * INTERVAL '180 days'),
            0,
            status_options[1 + floor(random() * 5)::INTEGER]
        )
        RETURNING id INTO order_uuid;

        -- Insert order items
        FOR j IN 1..item_count LOOP
            item_price := 10 + (random() * 990)::DECIMAL(12,2); -- $10-$1000
            item_quantity := 1 + floor(random() * 5)::INTEGER; -- 1-5 quantity

            INSERT INTO order_items (order_id, product_name, quantity, price)
            VALUES (
                order_uuid,
                'Product ' || ((i * 10 + j) % 200), -- Reuse product names for variety
                item_quantity,
                item_price
            );

            order_total := order_total + (item_price * item_quantity);
        END LOOP;

        -- Update order total
        UPDATE orders SET total_amount = order_total WHERE id = order_uuid;
    END LOOP;

    -- Report final counts
    SELECT COUNT(*) INTO existing_count FROM customers;
    RAISE NOTICE 'Data seeding complete:';
    RAISE NOTICE '  Customers: %', existing_count;

    SELECT COUNT(*) INTO existing_count FROM orders;
    RAISE NOTICE '  Orders: %', existing_count;

    SELECT COUNT(*) INTO existing_count FROM order_items;
    RAISE NOTICE '  Order Items: %', existing_count;
END $$;
