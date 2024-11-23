#pragma once

// Attention : Inclure GLAD avant GLFW
#include <glad/glad.h>
#include <GLFW/glfw3.h>
#include "../external/glm/ext/matrix_clip_space.hpp"
#include <imgui.h>
#include <imgui_impl_glfw.h>
#include <imgui_impl_opengl3.h>
#include <iostream>
#include <vector>
#include <cstring>
#include <string>
#include <fstream>
#include <sstream>
#include <cmath>
#include <stdbool.h>
#include <unistd.h>
#include <sys/sysinfo.h>
#include <thread>
#include "../external/glm/glm.hpp"
#include "../external/glm/gtc/matrix_transform.hpp"
#include "../external/glm/gtc/type_ptr.hpp"

#include <openxr/openxr.h>
#include <openxr/openxr_platform.h>

#include <Window.hpp>
#include <Shader.hpp>
#include <stb_image.h>
#include <stb_image_write.h>
#include <Texture.h>
#include <Skybox.hpp>

