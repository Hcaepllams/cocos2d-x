/****************************************************************************
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2009      Jason Booth

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/

#include "CCConfiguration.h"
#include "misc_nodes/CCRenderTexture.h"
#include "CCDirector.h"
#include "platform/platform.h"
#include "platform/CCImage.h"
#include "shaders/CCGLProgram.h"
#include "shaders/ccGLStateCache.h"
#include "CCConfiguration.h"
#include "support/ccUtils.h"
#include "textures/CCTextureCache.h"
#include "platform/CCFileUtils.h"
#include "CCGL.h"
#include "support/CCNotificationCenter.h"
#include "CCEventType.h"
#include "effects/CCGrid.h"
// extern
#include "kazmath/GL/matrix.h"

NS_CC_BEGIN

// implementation CCRenderTexture
CCRenderTexture::CCRenderTexture()
: m_pSprite(NULL)
, m_uFBO(0)
, m_uDepthRenderBufffer(0)
, m_nOldFBO(0)
, m_pTexture(0)
, m_pTextureCopy(0)
, m_pUITextureImage(NULL)
, m_ePixelFormat(kCCTexture2DPixelFormat_RGBA8888)
, m_uClearFlags(0)
, m_sClearColor(ccc4f(0,0,0,0))
, m_fClearDepth(0.0f)
, m_nClearStencil(0)
, m_bAutoDraw(false)
{
#if CC_ENABLE_CACHE_TEXTURE_DATA
    // Listen this event to save render texture before come to background.
    // Then it can be restored after coming to foreground on Android.
    CCNotificationCenter::sharedNotificationCenter()->addObserver(this,
                                                                  callfuncO_selector(CCRenderTexture::listenToBackground),
                                                                  EVENT_COME_TO_BACKGROUND,
                                                                  NULL);
    
    CCNotificationCenter::sharedNotificationCenter()->addObserver(this,
                                                                  callfuncO_selector(CCRenderTexture::listenToForeground),
                                                                  EVNET_COME_TO_FOREGROUND, // this is misspelt
                                                                  NULL);
#endif
}

CCRenderTexture::~CCRenderTexture()
{
    CC_SAFE_RELEASE(m_pSprite);
    CC_SAFE_RELEASE(m_pTextureCopy);
    
//cjh gl function here.;
    if (m_uDepthRenderBufffer)
    {
//cjh gl function here.;
    }
    CC_SAFE_DELETE(m_pUITextureImage);

#if CC_ENABLE_CACHE_TEXTURE_DATA
    CCNotificationCenter::sharedNotificationCenter()->removeObserver(this, EVENT_COME_TO_BACKGROUND);
    CCNotificationCenter::sharedNotificationCenter()->removeObserver(this, EVNET_COME_TO_FOREGROUND);
#endif
}

void CCRenderTexture::listenToBackground(cocos2d::CCObject *obj)
{
#if CC_ENABLE_CACHE_TEXTURE_DATA
    CC_SAFE_DELETE(m_pUITextureImage);
    
    // to get the rendered texture data
    m_pUITextureImage = newCCImage(false);

    if (m_pUITextureImage)
    {
        const CCSize& s = m_pTexture->getContentSizeInPixels();
        VolatileTexture::addDataTexture(m_pTexture, m_pUITextureImage->getData(), kTexture2DPixelFormat_RGBA8888, s);
        
        if ( m_pTextureCopy )
        {
            VolatileTexture::addDataTexture(m_pTextureCopy, m_pUITextureImage->getData(), kTexture2DPixelFormat_RGBA8888, s);
        }
    }
    else
    {
        CCLOG("Cache rendertexture failed!");
    }
    
//cjh gl function here.;
    m_uFBO = 0;
#endif
}

void CCRenderTexture::listenToForeground(cocos2d::CCObject *obj)
{
#if CC_ENABLE_CACHE_TEXTURE_DATA
    // -- regenerate frame buffer object and attach the texture
//cjh gl function here.;
    
//cjh gl function here.;
//cjh gl function here.;
    
    m_pTexture->setAliasTexParameters();
    
    if ( m_pTextureCopy )
    {
        m_pTextureCopy->setAliasTexParameters();
    }
    
//cjh gl function here.;
//cjh gl function here.;
#endif
}

CCSprite * CCRenderTexture::getSprite()
{
    return m_pSprite;
}

void CCRenderTexture::setSprite(CCSprite* var)
{
    CC_SAFE_RELEASE(m_pSprite);
    m_pSprite = var;
    CC_SAFE_RETAIN(m_pSprite);
}

unsigned int CCRenderTexture::getClearFlags() const
{
    return m_uClearFlags;
}

void CCRenderTexture::setClearFlags(unsigned int uClearFlags)
{
    m_uClearFlags = uClearFlags;
}

const ccColor4F& CCRenderTexture::getClearColor() const
{
    return m_sClearColor;
}

void CCRenderTexture::setClearColor(const ccColor4F &clearColor)
{
    m_sClearColor = clearColor;
}

float CCRenderTexture::getClearDepth() const
{
    return m_fClearDepth;
}

void CCRenderTexture::setClearDepth(float fClearDepth)
{
    m_fClearDepth = fClearDepth;
}

int CCRenderTexture::getClearStencil() const
{
    return m_nClearStencil;
}

void CCRenderTexture::setClearStencil(float fClearStencil)
{
    m_nClearStencil = fClearStencil;
}

bool CCRenderTexture::isAutoDraw() const
{
    return m_bAutoDraw;
}

void CCRenderTexture::setAutoDraw(bool bAutoDraw)
{
    m_bAutoDraw = bAutoDraw;
}

CCRenderTexture * CCRenderTexture::create(int w, int h, CCTexture2DPixelFormat eFormat)
{
    CCRenderTexture *pRet = new CCRenderTexture();

    if(pRet && pRet->initWithWidthAndHeight(w, h, eFormat))
    {
        pRet->autorelease();
        return pRet;
    }
    CC_SAFE_DELETE(pRet);
    return NULL;
}

CCRenderTexture * CCRenderTexture::create(int w ,int h, CCTexture2DPixelFormat eFormat, GLuint uDepthStencilFormat)
{
    CCRenderTexture *pRet = new CCRenderTexture();

    if(pRet && pRet->initWithWidthAndHeight(w, h, eFormat, uDepthStencilFormat))
    {
        pRet->autorelease();
        return pRet;
    }
    CC_SAFE_DELETE(pRet);
    return NULL;
}

CCRenderTexture * CCRenderTexture::create(int w, int h)
{
    CCRenderTexture *pRet = new CCRenderTexture();

    if(pRet && pRet->initWithWidthAndHeight(w, h, kCCTexture2DPixelFormat_RGBA8888, 0))
    {
        pRet->autorelease();
        return pRet;
    }
    CC_SAFE_DELETE(pRet);
    return NULL;
}

bool CCRenderTexture::initWithWidthAndHeight(int w, int h, CCTexture2DPixelFormat eFormat)
{
    return initWithWidthAndHeight(w, h, eFormat, 0);
}

bool CCRenderTexture::initWithWidthAndHeight(int w, int h, CCTexture2DPixelFormat eFormat, GLuint uDepthStencilFormat)
{
    CCAssert(eFormat != kCCTexture2DPixelFormat_A8, "only RGB and RGBA formats are valid for a render texture");

    bool bRet = false;
    void *data = NULL;
    do 
    {
        w = (int)(w * CC_CONTENT_SCALE_FACTOR());
        h = (int)(h * CC_CONTENT_SCALE_FACTOR());

//cjh gl function here.;

        // textures must be power of two squared
        unsigned int powW = 0;
        unsigned int powH = 0;

        if (CCConfiguration::sharedConfiguration()->supportsNPOT())
        {
            powW = w;
            powH = h;
        }
        else
        {
            powW = ccNextPOT(w);
            powH = ccNextPOT(h);
        }

        data = malloc((int)(powW * powH * 4));
        CC_BREAK_IF(! data);

        memset(data, 0, (int)(powW * powH * 4));
        m_ePixelFormat = eFormat;

        m_pTexture = new CCTexture2D();
        if (m_pTexture)
        {
            m_pTexture->initWithData(data, (CCTexture2DPixelFormat)m_ePixelFormat, powW, powH, CCSizeMake((float)w, (float)h));
        }
        else
        {
            break;
        }
        GLint oldRBO;
//cjh gl function here.;
        
        if (CCConfiguration::sharedConfiguration()->checkForGLExtension("GL_QCOM"))
        {
            m_pTextureCopy = new CCTexture2D();
            if (m_pTextureCopy)
            {
                m_pTextureCopy->initWithData(data, (CCTexture2DPixelFormat)m_ePixelFormat, powW, powH, CCSizeMake((float)w, (float)h));
            }
            else
            {
                break;
            }
        }

        // generate FBO
//cjh gl function here.;
//cjh gl function here.;

        // associate texture with FBO
//cjh gl function here.;

        if (uDepthStencilFormat != 0)
        {
            //create and attach depth buffer
//cjh gl function here.;
//cjh gl function here.;
//cjh gl function here.;
//cjh gl function here.;

            // if depth format is the one with stencil part, bind same render buffer as stencil attachment
//            if (uDepthStencilFormat == GL_DEPTH24_STENCIL8)
            {
//cjh gl function here.;
            }
        }

        // check if it worked (probably worth doing :) )
//cjh gl function here.;

        m_pTexture->setAliasTexParameters();

        // retained
        setSprite(CCSprite::createWithTexture(m_pTexture));

        m_pTexture->release();
        m_pSprite->setScaleY(-1);

        ccBlendFunc tBlendFunc = {GL_ONE, GL_ONE_MINUS_SRC_ALPHA };
        m_pSprite->setBlendFunc(tBlendFunc);

//cjh gl function here.;
//cjh gl function here.;
        
        // Diabled by default.
        m_bAutoDraw = false;
        
        // add sprite for backward compatibility
        addChild(m_pSprite);
        
        bRet = true;
    } while (0);
    
    CC_SAFE_FREE(data);
    
    return bRet;
}

void CCRenderTexture::begin()
{
    kmGLMatrixMode(KM_GL_PROJECTION);
	kmGLPushMatrix();
	kmGLMatrixMode(KM_GL_MODELVIEW);
    kmGLPushMatrix();
    
    CCDirector *director = CCDirector::sharedDirector();
    director->setProjection(director->getProjection());

    const CCSize& texSize = m_pTexture->getContentSizeInPixels();

    // Calculate the adjustment ratios based on the old and new projections
    CCSize size = director->getWinSizeInPixels();
    float widthRatio = size.width / texSize.width;
    float heightRatio = size.height / texSize.height;

    // Adjust the orthographic projection and viewport
//cjh gl function here.;


    kmMat4 orthoMatrix;
    kmMat4OrthographicProjection(&orthoMatrix, (float)-1.0 / widthRatio,  (float)1.0 / widthRatio,
        (float)-1.0 / heightRatio, (float)1.0 / heightRatio, -1,1 );
    kmGLMultMatrix(&orthoMatrix);

//cjh gl function here.;
//cjh gl function here.;
    
    /*  Certain Qualcomm Andreno gpu's will retain data in memory after a frame buffer switch which corrupts the render to the texture. The solution is to clear the frame buffer before rendering to the texture. However, calling glClear has the unintended result of clearing the current texture. Create a temporary texture to overcome this. At the end of CCRenderTexture::begin(), switch the attached texture to the second one, call glClear, and then switch back to the original texture. This solution is unnecessary for other devices as they don't have the same issue with switching frame buffers.
     */
    if (CCConfiguration::sharedConfiguration()->checkForGLExtension("GL_QCOM"))
    {
        // -- bind a temporary texture so we can clear the render buffer without losing our texture
//cjh gl function here.;
        CHECK_GL_ERROR_DEBUG();
//cjh gl function here.;
//cjh gl function here.;
    }
}

void CCRenderTexture::beginWithClear(float r, float g, float b, float a)
{
 //   beginWithClear(r, g, b, a, 0, 0, GL_COLOR_BUFFER_BIT);
}

void CCRenderTexture::beginWithClear(float r, float g, float b, float a, float depthValue)
{
//    beginWithClear(r, g, b, a, depthValue, 0, GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
}

void CCRenderTexture::beginWithClear(float r, float g, float b, float a, float depthValue, int stencilValue)
{
 //   beginWithClear(r, g, b, a, depthValue, stencilValue, GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);
}

void CCRenderTexture::beginWithClear(float r, float g, float b, float a, float depthValue, int stencilValue, GLbitfield flags)
{
    this->begin();

    // save clear color
//     GLfloat	clearColor[4] = {0.0f};
//     GLfloat depthClearValue = 0.0f;
//     int stencilClearValue = 0;
//     
//     if (flags & GL_COLOR_BUFFER_BIT)
//     {
// //cjh gl function here.;
// //cjh gl function here.;
//     }
//     
//     if (flags & GL_DEPTH_BUFFER_BIT)
//     {
// //cjh gl function here.;
// //cjh gl function here.;
//     }
// 
//     if (flags & GL_STENCIL_BUFFER_BIT)
//     {
// //cjh gl function here.;
// //cjh gl function here.;
//     }
//     
// //cjh gl function here.;
// 
//     // restore
//     if (flags & GL_COLOR_BUFFER_BIT)
//     {
// //cjh gl function here.;
//     }
//     if (flags & GL_DEPTH_BUFFER_BIT)
//     {
// //cjh gl function here.;
//     }
//     if (flags & GL_STENCIL_BUFFER_BIT)
//     {
// //cjh gl function here.;
//     }
}

void CCRenderTexture::end()
{
    CCDirector *director = CCDirector::sharedDirector();
    
//cjh gl function here.;

    // restore viewport
    director->setViewport();

    kmGLMatrixMode(KM_GL_PROJECTION);
	kmGLPopMatrix();
	kmGLMatrixMode(KM_GL_MODELVIEW);
	kmGLPopMatrix();
}

void CCRenderTexture::clear(float r, float g, float b, float a)
{
    this->beginWithClear(r, g, b, a);
    this->end();
}

void CCRenderTexture::clearDepth(float depthValue)
{
    this->begin();
    //! save old depth value
    GLfloat depthClearValue;
//cjh gl function here.;

//cjh gl function here.;
//cjh gl function here.;

    // restore clear color
//cjh gl function here.;
    this->end();
}

void CCRenderTexture::clearStencil(int stencilValue)
{
    // save old stencil value
    int stencilClearValue;
//cjh gl function here.;

//cjh gl function here.;
//cjh gl function here.;

    // restore clear color
//cjh gl function here.;
}

void CCRenderTexture::visit()
{
    // override visit.
	// Don't call visit on its children
    if (!m_bVisible)
    {
        return;
    }
	
	kmGLPushMatrix();
	
    if (m_pGrid && m_pGrid->isActive())
    {
        m_pGrid->beforeDraw();
        transformAncestors();
    }
    
    transform();
    m_pSprite->visit();
    draw();
	
    if (m_pGrid && m_pGrid->isActive())
    {
        m_pGrid->afterDraw(this);
    }
	
	kmGLPopMatrix();

    m_uOrderOfArrival = 0;
}

void CCRenderTexture::draw()
{
    if( m_bAutoDraw)
    {
        begin();
		
        if (m_uClearFlags)
        {
            GLfloat oldClearColor[4] = {0.0f};
			GLfloat oldDepthClearValue = 0.0f;
			GLint oldStencilClearValue = 0;
			
			// backup and set
// 			if (m_uClearFlags & GL_COLOR_BUFFER_BIT)
//             {
// //cjh gl function here.;
// //cjh gl function here.;
// 			}
// 			
// 			if (m_uClearFlags & GL_DEPTH_BUFFER_BIT)
//             {
// //cjh gl function here.;
// //cjh gl function here.;
// 			}
// 			
// 			if (m_uClearFlags & GL_STENCIL_BUFFER_BIT)
//             {
// //cjh gl function here.;
// //cjh gl function here.;
// 			}
// 			
// 			// clear
// //cjh gl function here.;
// 			
// 			// restore
// 			if (m_uClearFlags & GL_COLOR_BUFFER_BIT)
//             {
// //cjh gl function here.;
//             }
// 			if (m_uClearFlags & GL_DEPTH_BUFFER_BIT)
//             {
// //cjh gl function here.;
//             }
// 			if (m_uClearFlags & GL_STENCIL_BUFFER_BIT)
//             {
// //cjh gl function here.;
//             }
		}
		
		//! make sure all children are drawn
        sortAllChildren();
		
		CCObject *pElement;
		CCARRAY_FOREACH(m_pChildren, pElement)
        {
            CCNode *pChild = (CCNode*)pElement;

            if (pChild != m_pSprite)
            {
                pChild->visit();
            }
		}
        
        end();
	}
}

bool CCRenderTexture::saveToFile(const char *szFilePath)
{
    bool bRet = false;

    CCImage *pImage = newCCImage(true);
    if (pImage)
    {
        bRet = pImage->saveToFile(szFilePath, kCCImageFormatJPEG);
    }

    CC_SAFE_DELETE(pImage);
    return bRet;
}
bool CCRenderTexture::saveToFile(const char *fileName, tCCImageFormat format)
{
    bool bRet = false;
    CCAssert(format == kCCImageFormatJPEG || format == kCCImageFormatPNG,
             "the image can only be saved as JPG or PNG format");

    CCImage *pImage = newCCImage(true);
    if (pImage)
    {
        std::string fullpath = CCFileUtils::sharedFileUtils()->getWritablePath() + fileName;
        
        bRet = pImage->saveToFile(fullpath.c_str(), true);
    }

    CC_SAFE_DELETE(pImage);

    return bRet;
}

/* get buffer as CCImage */
CCImage* CCRenderTexture::newCCImage(bool flipImage)
{
    CCAssert(m_ePixelFormat == kCCTexture2DPixelFormat_RGBA8888, "only RGBA8888 can be saved as image");

    if (NULL == m_pTexture)
    {
        return NULL;
    }

    const CCSize& s = m_pTexture->getContentSizeInPixels();

    // to get the image size to save
    //        if the saving image domain exceeds the buffer texture domain,
    //        it should be cut
    int nSavedBufferWidth = (int)s.width;
    int nSavedBufferHeight = (int)s.height;

    GLubyte *pBuffer = NULL;
    GLubyte *pTempData = NULL;
    CCImage *pImage = new CCImage();

    do
    {
        CC_BREAK_IF(! (pBuffer = new GLubyte[nSavedBufferWidth * nSavedBufferHeight * 4]));

        if(! (pTempData = new GLubyte[nSavedBufferWidth * nSavedBufferHeight * 4]))
        {
            delete[] pBuffer;
            pBuffer = NULL;
            break;
        }

        this->begin();
//cjh gl function here.;
//cjh gl function here.;
        this->end();

        if ( flipImage ) // -- flip is only required when saving image to file
        {
            // to get the actual texture data
            // #640 the image read from rendertexture is dirty
            for (int i = 0; i < nSavedBufferHeight; ++i)
            {
                memcpy(&pBuffer[i * nSavedBufferWidth * 4], 
                       &pTempData[(nSavedBufferHeight - i - 1) * nSavedBufferWidth * 4], 
                       nSavedBufferWidth * 4);
            }

            pImage->initWithImageData(pBuffer, nSavedBufferWidth * nSavedBufferHeight * 4, CCImage::kFmtRawData, nSavedBufferWidth, nSavedBufferHeight, 8);
        }
        else
        {
            pImage->initWithImageData(pTempData, nSavedBufferWidth * nSavedBufferHeight * 4, CCImage::kFmtRawData, nSavedBufferWidth, nSavedBufferHeight, 8);
        }
        
    } while (0);

    CC_SAFE_DELETE_ARRAY(pBuffer);
    CC_SAFE_DELETE_ARRAY(pTempData);

    return pImage;
}

NS_CC_END
