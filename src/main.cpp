#include <Headers.hpp>

#define SCREEN_WIDTH 1280
#define SCREEN_HEIGHT 720

float deltaTime = 0.0f;	
float lastFrame = 0.0f;

// Caméra
glm::vec3 camera_position  = glm::vec3(0.0f, 5.0f, 5.0f);
glm::vec3 camera_target = glm::vec3(1.0f,0.0f,-1.0f);
glm::vec3 camera_up = glm::vec3(0.0f, 1.0f,  0.0f);

bool cameraMouseLibre = false;

int speedCam = 60;
double previousX = SCREEN_WIDTH / 2;
double previousY = SCREEN_HEIGHT / 2;
bool firstMouse = true;
float phi = -90.0f;
float theta = 0.0f;
float FoV = 75.0f;

void framebuffer_size_callback(GLFWwindow* window, int width, int height){ 
    glViewport(0,0,width,height);
}

void processInput(GLFWwindow* window){ 
    float camera_speed = (float)speedCam * deltaTime;

    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS){
        glfwSetWindowShouldClose(window,true);
    }
}

void mouse_cursor_callback(GLFWwindow* window, double xpos, double ypos){
    
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // Pour masquer la souris sur la fenêtre

    if (firstMouse){
        previousX = xpos;
        previousY = ypos;
        firstMouse = false;
    }

    double deltaX = xpos - previousX;
    double deltaY = previousY - ypos;

    phi += deltaX*0.05f;
    theta += deltaY*0.05f;
    theta = glm::clamp(theta, -89.0f, 89.0f);

    float x = cos(glm::radians(phi)) * cos(glm::radians(theta));
    float y = sin(glm::radians(theta));
    float z = sin(glm::radians(phi)) * cos(glm::radians(theta));
    camera_target = glm::normalize(glm::vec3(x,y,z));

    previousX=xpos;
    previousY=ypos;

}

XrInstance instance;
XrInstanceCreateInfo createInfo = {XR_TYPE_INSTANCE_CREATE_INFO};

int main(){

    strcpy(createInfo.applicationInfo.applicationName, "MinecraftVR");
    createInfo.applicationInfo.applicationVersion = 1;
    snprintf(createInfo.applicationInfo.engineName, XR_MAX_ENGINE_NAME_SIZE, "VoxelImagineEngine");
    createInfo.applicationInfo.engineVersion = 1;
    createInfo.applicationInfo.apiVersion = XR_CURRENT_API_VERSION;

    XrResult result = xrCreateInstance(&createInfo, &instance);

    if (result != XR_SUCCESS) {
        std::cerr << "Failed to create OpenXR instance: " << result << std::endl;
        return -1;
    }

    std::cout << "OpenXR instance created successfully!" << std::endl;

    xrDestroyInstance(instance);

    if( !glfwInit()){
        fprintf( stderr, "Failed to initialize GLFW\n" );
        //getchar();
        return -1;
    }
    glfwWindowHint(GLFW_SAMPLES, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    
    Window *window_object = new Window(3, 3, SCREEN_WIDTH, SCREEN_HEIGHT, "Projet VR");
    window_object->setup_GLFW();
    GLFWwindow* window = window_object->get_window();
    glfwMakeContextCurrent(window);

    glfwSetFramebufferSizeCallback(window, framebuffer_size_callback); // On définit le callback à appeler lors du redimensionnement de la fenêtre
    glClearColor(0.36f, 0.61f, 1.0f, 1.0f);

    // Enable depth test
    glEnable(GL_DEPTH_TEST);
    // Accept fragment if it closer to the camera than the former one
    glDepthFunc(GL_LESS);
    glEnable(GL_CULL_FACE); // Attention à la construction des triangles
    // Pour utiliser de la transparence dans le fragment shader (par exemple pour le bloc de glace)
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
    glfwSetCursorPosCallback(window, mouse_cursor_callback);

    GLuint VertexArrayID;
    glGenVertexArrays(1, &VertexArrayID);
    glBindVertexArray(VertexArrayID);

    Skybox *skybox = new Skybox();

    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    skybox->bindCubemap(GL_TEXTURE3, 3); 


    // Boucle de rendu
    while(!glfwWindowShouldClose(window)){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        processInput(window);

        glm::mat4 Model = glm::mat4(1.0f);
        glm::mat4 Projection = glm::perspective(glm::radians(FoV), (float)SCREEN_WIDTH/(float)SCREEN_HEIGHT,0.1f,2000.0f);
        glm::mat4 View = glm::lookAt(camera_position, camera_position + camera_target, camera_up);

        // Affichage de la skybox
        skybox->drawSkybox(Model, Projection, View);

        float currentFrame = glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    glDeleteVertexArrays(1, &VertexArrayID);

    delete skybox;
    delete window_object;
    
    glfwTerminate();
    return 0;
}
