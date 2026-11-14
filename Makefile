FRONTEND_DIR = frontend
BACKEND_DIR = backend

.PHONY: dev front back

dev:
	$(MAKE) -j 2 front back

front:
	cd $(FRONTEND_DIR) && npm run dev

back:
	cd $(BACKEND_DIR) && ./mvnw spring-boot:run
