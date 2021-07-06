#include "GLUtils.h"
#include "LogUtils.h"
#include <vector>
#define LOG_TAG "FeatureUtils/GLUtils"

#ifdef FT_AI_CAMERA_FEATURE
GLuint GLUtils::loadShader(GLenum shaderType, const char *pSource) {
    GLuint shader = glCreateShader(shaderType);
    //checkGlError("glCreateShader");
    if (shader == 0) {
        MY_LOGE("loadShader error\n");
        return shader;
    }

    glShaderSource(shader, 1, &pSource, NULL);
    glCompileShader(shader);
    GLint compiled = GL_FALSE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    //checkGlError("glGetShaderiv");
    if (!compiled) {
        GLint infoLength = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLength);
        if (infoLength > 0) {
            std::vector<char> info(infoLength, '\0');
            glGetShaderInfoLog(shader, infoLength, NULL, info.data());
            MY_LOGD("Could not compile shader %d:\n%s\n", shaderType, info.data());
            glDeleteShader(shader);
            shader = 0;
        }
    }
    //MY_LOGD("loadShader shader:%d\n",shader);
    return shader;
}

GLuint GLUtils::createProgram(const char *pVertexSource, const char *pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        MY_LOGE("createProgram load vertexShader error\n");
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        MY_LOGE("createProgram load pixelShader error\n");
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program == 0) {
        MY_LOGE("createProgramerror\n");
        return program;
    }

    glAttachShader(program, vertexShader);
    //checkGlError("glAttachShader");
    glAttachShader(program, pixelShader);
    //checkGlError("glAttachShader");
    glLinkProgram(program);
    //checkGlError("glLinkProgram");
    GLint linkStatus = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    //checkGlError("glGetProgramiv");
    if (!linkStatus) {
        GLint infoLength = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLength);
        if (infoLength > 0) {
            std::vector<char> info(infoLength, '\0');
            glGetProgramInfoLog(program, infoLength, NULL, info.data());
            MY_LOGE("Could not link program:\n%s\n", info.data());
        }
        glDeleteProgram(program);
        program = 0;
    }
    //MY_LOGD("createProgram program:%d\n",program);
    return program;
}

void GLUtils::checkGlError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        MY_LOGD("after %s() glError (0x%x)\n", op, error);
    }
}
#endif

GLuint GLUtils::generateTexureOES(GLint mode)
{
    FUNCTION_IN;
    GLuint texture = 0;

    // allocate a texture name
    glGenTextures( 1, &texture );

    // select our current texture
    glBindTexture( GL_TEXTURE_EXTERNAL_OES, texture );

    // when texture area is small, bilinear filter the closest MIP map
    glTexParameterf( GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, mode );//NEAREST
    // when texture area is large, bilinear filter the first MIP map
    glTexParameterf( GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, mode );//NEAREST

    // if wrap is true, the texture wraps over at the edges (repeat)
    //       ... false, the texture ends at the edges (clamp)
    glTexParameteri( GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri( GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    //checkGlError("glTexParameteri");
    FUNCTION_OUT;
    return texture;
}

#ifdef FT_AI_CAMERA_FEATURE
GLuint GLUtils::generateNormalTexture(int filter, int wrap, int width, int height)
{
    FUNCTION_IN;

    GLuint texture;

    glGenTextures(1, &texture);
    //checkGlError("glGenTextures");
    glBindTexture(GL_TEXTURE_2D, texture);
    //checkGlError("glBindTexture");
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);
    //checkGlError("glTexImage2D");
    // when texture area is small, bilinear filter the closest MIP map
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter );//NEAREST
    //checkGlError("glTexParameterf");
    // when texture area is large, bilinear filter the first MIP map
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter );//NEAREST
    //checkGlError("glTexParameterf");
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
    //checkGlError("glTexParameteri");
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap );
    //checkGlError("glTexParameteri");

    FUNCTION_OUT;
    return texture;
}
#endif

void GLUtils::getEGLImageTexture(GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS *params)
{
    FUNCTION_IN;
    // create EGLImage
    static EGLClientBuffer clientBuffer;
    clientBuffer  = eglGetNativeClientBufferANDROID(params->graphicBuffer);
    params->eglImage= eglCreateImageKHR(params->eglDisplay, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, clientBuffer, 0);

    // bind EGLImage to texture
    params->textureID = generateTexureOES(GL_LINEAR);
    glBindTexture(GL_TEXTURE_EXTERNAL_OES, params->textureID); //GL_TEXTURE_EXTERNAL_OES
    glEGLImageTargetTexture2DOES(GL_TEXTURE_EXTERNAL_OES, params->eglImage); // bind eglImage to texture

    if (params->isRenderTarget)
    {
        glGenFramebuffers(1, &(params->fbo));
        glBindFramebuffer(GL_FRAMEBUFFER, params->fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, params->textureID, 0);
        glCheckFramebufferStatus(GL_FRAMEBUFFER);
    }
    FUNCTION_OUT;
}

void GLUtils::releaseEGLImageTexture(GLUTILS_GET_EGLIMAGE_TEXTURE_PARAMS *params)
{
    FUNCTION_IN;

    if (params->textureID) {
        GLuint texture[1];
        texture[0] = params->textureID;
        glDeleteTextures(1, texture);
    }

    if (params->eglImage) {
        eglDestroyImageKHR(params->eglDisplay, params->eglImage);
    }

    if (params->isRenderTarget) {
        GLuint fbo[1];
        fbo[0] = params->fbo;
        glDeleteFramebuffers(1, fbo);
    }

    FUNCTION_OUT;
}
