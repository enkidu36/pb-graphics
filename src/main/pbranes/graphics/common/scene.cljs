(ns pbranes.graphics.common.scene 
  (:require
    [pbranes.graphics.common.utils :as u]))

(defprotocol WebGlScene
  (get-by-alias [_ alias])
   (load [_ filename alias attribrutes] )
   (loadByParts [_ path count alias])
   (add [_ object attributes])
   (traverse [_ cb])
   (remove-by-alias [_ alias])
   (renderLast [_ alias])
   (renderSooner [_ alias])
   (renderLater [_ alias])
   (printRenderOrder [this])
  )

(defrecord Scene [gl program objects]
  WebGlScene
  (get-by-alias [_ alias] (js/console.log "get"))
  (load [_ filename alias attributes] (js/console.log "load"))
  (loadByParts [_ path count alias] (js/console.log "loadByParts"))

  (add [_ object attributes]

    (js/console.group "Scene Add")
    (js/console.log "program: " program)
    (js/console.log "object verticies: " (:vertices object))
    (js/console.log "attributes: " attributes)
    (js/console.log "gl: " gl)

    (let [obj (-> object
                  (assoc :ibo (.createBuffer gl))
                  (assoc :vao (.createVertexArray gl)))]

      (.bindVertexArray gl (:vao obj))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;; Indices
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) (:ibo obj))
      (.bufferData gl (.-ELEMENT_ARRAY_BUFFER gl) (js/Uint16Array. (:indices obj)) (.-STATIC_DRAW gl))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;; Vertex Positions
      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      (when (>= (get program "aVertexPosition") 0)
        (let [vertex-position (get program "aVertexPosition")
              vertex-buffer (.createBuffer gl)]
          (.bindBuffer gl (.-ARRAY_BUFFER gl) vertex-buffer)
          (.bufferData gl (.-ARRAY_BUFFER gl) (js/Float32Array. (:vertices obj)) (.-STATIC_DRAW gl))

          (.enableVertexAttribArray gl vertex-position)
          (.vertexAttribPointer gl vertex-position 3 (.-FLOAT gl) false 0 0)))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;; Vertex Normals
      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      (when (>= (get program "aVertexNormal") 0)
        (let [vertex-normal (get program "aVertexNormal")
              normal-buffer (.createBuffer gl)
              normals (js/utils.calculateNormals (clj->js (:vertices obj)) (:indices obj))]
          (.bindBuffer gl (.-ARRAY_BUFFER gl) normal-buffer)
          (.bufferData gl (.-ARRAY_BUFFER gl) (js/Float32Array. normals) (.-STATIC_DRAW gl))
          (.enableVertexAttribArray gl vertex-normal)
          (.vertexAttribPointer gl vertex-normal 3 (.-FLOAT gl) false 0 0)))

      (swap! objects conj obj))

    (u/clear-all-arrays-buffers gl)

    (js/console.groupEnd))

  (traverse [_ cd] (js/console.log "traverse"))
  (remove-by-alias [_ alias] (js/console.log "remove"))
  (renderLast [_ alias] (js/console.log "renderLast"))
  (renderSooner [_ alias] (js/console.log "renderSooner"))
  (renderLater [_ alias] (js/console.log "renderLater"))
  (printRenderOrder [this]
    (get-by-alias this "alias")
    (load this "filename" "alias" "attributes")
    (loadByParts this "path" "count" "alias")
    (add this "object" "attributes")
    (traverse this "")
    (remove-by-alias this "alias")
    (renderLast this "alias")
    (renderSooner this "alias")
    (renderLater this "alias")))



