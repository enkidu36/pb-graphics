(ns pbranes.graphics.page.lighting
  (:require [helix.core :refer [defnc]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            ["dat.gui" :as dg]
            ["gl-matrix" :as glmatrix]
            [pbranes.graphics.geometry.ball :refer [vertices indices]]
            [pbranes.graphics.common.utils :as u]))
(set! *warn-on-infer* false)

(def vs-shader
  "#version 300 es
precision mediump float;

uniform mat4 uModelViewMatrix;
uniform mat4 uProjectionMatrix;
uniform mat4 uNormalMatrix;
uniform vec3 uLightDirection;
uniform vec3 uLightDiffuse;
uniform vec3 uMaterialDiffuse;

in vec3 aVertexPosition;
in vec3 aVertexNormal;

out vec4 vVertexColor;

void main(void) {
  // Calculate the normal vector
  vec3 N = normalize(vec3(uNormalMatrix * vec4(aVertexNormal, 1.0)));
  
  // Normalize the light direction
  vec3 L = normalize(uLightDirection);

  // Dot product of the normal product and negative light direction vector
  float lambertTerm = dot(N, -L);
 
  // Calculating the diffuse color based on the Labertian reflection model
  vec3 Id = uMaterialDiffuse * uLightDiffuse * lambertTerm;
 
  // Set the varying to be used inside of the fragment vs-shader
  vVertexColor = vec4(1.0, 0.0, 0.0 , 1.0);

  // Setting the vertex position
  gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aVertexPosition, 1.0);
}
")

(def fs-shader
  "#version 300 es
precision mediump float;

  // Expect the interpolated value from the vertex fs-shader
  in vec4 vVertexColor;
  
  // Return the final value as a fragColor
  out vec4 fragColor;

  void main(void) {
    
    fragColor = vVertexColor;
  }
")

(defn init-program [gl]
  (u/auto-resize-canvas (.-canvas gl))
  (.clearColor gl 0.9 0.9 0.9 1.0)
  (.enable gl (.-DEPTH_TEST gl))

  (let [program (.createProgram gl)
        vertex-shader (u/compile-shader gl vs-shader (.-VERTEX_SHADER gl))
        fragment-shader (u/compile-shader gl fs-shader (.-FRAGMENT_SHADER gl))]

    (.attachShader gl program vertex-shader)
    (.attachShader gl program fragment-shader)
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program (.-LINK_STATUS gl)))
      (js/console.error  "Could not initialize shaders"))

    (.useProgram gl program)

    ;; Set locations onto the "program" instance

    (set! (.-aVertexPosition gl) (.getAttribLocation gl program "aVertexPosition"))
    (set! (.-aVertexNormal gl) (.getAttribLocation gl program "aVertexNormal"))
    (set! (.-uProjectionMatrix gl) (.getUniformLocation gl program "uProjectionMatrix"))
    (set! (.-uModelMatrix gl) (.getUniformLocation gl program "uModelMatrix"))
    (set! (.-uNormalMatrix gl) (.getUniformLocation gl program "uNormalMatrix"))
    (set! (.-uMaterialDiffuse gl) (.getUniformLocation gl program "uMaterialDiffuse"))
    (set! (.-uLightDiffuse gl) (.getUniformLocation gl program "uLightDiffuse"))
    (set! (.-LightDirection gl) (.getUniformLocation gl program "uLightDirection"))

    program))


(defn init-buffers [program]

  (js/console.log "init-buffers")
  )

(defn init [gl controls]
  (let [program (init-program gl)]
    (init-buffers program))
  )

(defnc lighting-page []
  (let [canvas (hooks/use-ref nil)]

    (hooks/use-effect
      :once
      (let [gl (u/get-context canvas)
            controls (dg/GUI.)]
        (init gl controls)

        (fn unmount []
          (.destroy (.getRoot controls)))))

    (d/div
           (d/canvas {:ref canvas :className "webgl-canvas" :height 600 :width 800}
        "Your browser does not support HTML5 canvas."))))
