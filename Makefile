dev:
	make macos-arm

macos-arm:
	clj -M:macos-arm -m com.sonyahon.carbon.core $(args)

macos:
	clj -M:macos -m com.sonyahon.carbon.core $(args)
