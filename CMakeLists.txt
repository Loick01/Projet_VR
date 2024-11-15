cmake_minimum_required(VERSION 3.10)

# Always generate compile_commands.json for clangd
set(CMAKE_EXPORT_COMPILE_COMMANDS ON CACHE INTERNAL "")

# Project information
project(Projet_VR VERSION 1.0)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# Disable GLFW docs, tests, and examples to avoid unnecessary builds
set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

# Third-party libraries
# GLFW for windowing
add_subdirectory(external/glfw-3.3.9)

# GLM
add_subdirectory(external/glm)

# GLAD for OpenGL function loading
add_library(glad STATIC external/glad/src/glad.c)
target_include_directories(glad PRIVATE external/glad/include)

# ImGui setup
set(IMGUI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/external/imgui-1.90.4)
file(GLOB IMGUI_SOURCES ${IMGUI_DIR}/src/*.cpp)
add_library(imgui STATIC ${IMGUI_SOURCES})
target_include_directories(imgui PUBLIC ${IMGUI_DIR}/include ${IMGUI_DIR}/backends)

# OpenXR setup
set(OpenXR_SDK_PATH ${CMAKE_CURRENT_SOURCE_DIR}/external/OpenXR-SDK)
include_directories(${OpenXR_SDK_PATH}/include)
link_directories(${OpenXR_SDK_PATH}/build/src/loader)



# Find and link OpenGL
find_package(OpenGL REQUIRED)
target_link_libraries(imgui PUBLIC ${OPENGL_gl_LIBRARY} glfw)

# ImGui GLFW and OpenGL3 implementations
target_sources(imgui PUBLIC 
    ${IMGUI_DIR}/backends/imgui_impl_glfw.cpp 
    ${IMGUI_DIR}/backends/imgui_impl_opengl3.cpp
)

# Executable definition
add_executable(main
    src/main.cpp
    src/Window.cpp
    src/Shader.cpp
    src/Texture.cpp
    src/stb_image.cpp
    src/stb_image_write.cpp
    src/Skybox.cpp
    )

# Include directories for our project
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    external/glfw-3.3.9/include
    external/glad/include
)

# Linking libraries
target_link_libraries(main glfw glad imgui ${OpenXR_SDK_PATH}/build/src/loader/libopenxr_loader.so)

# Output directories
set_target_properties(glad imgui PROPERTIES
    ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)
