(ns pbranes.graphics.common.dgutils
  (:require ["dat.gui" :as dg]))

(set! *warn-on-infer* false)

(defn normalize-color
  "Normalize color map values to 0 - 1"
  [color]
  (mapv #(/ % 255) color))

(defn de-normalize-color
  "De-normalize color map values back to 0 - 255"
  [color]
  (mapv #(* % 255) color))

(defn folder?
  "Setting is a folder when it is a map and has no value"
  [setting]
  (and (map? setting) (nil? (:value setting))))

(defn action?
  "Setting is an action when it is a function"
  [setting]
  (fn? setting))

(defn color?
  "Setting is a color when there is a '#' or is a vector size greater than 3"
  [setting]
  (or
   (and (string? setting) (re-find #"#" setting))
   (and (vector? setting) (>= 3 (count setting)))))

(defn configure-controls
  "Declare function so can be used recursively.  Defined below"
  [_ _ _])

(defn create-controller
  "Create and add datGUI controllers"
  [gui setting state]

  (let [setting-key (first setting)
        setting-value (second setting)]

    (cond
      (action? setting-value)
      (.add gui (clj->js @(swap! state assoc setting-key setting-value)))

      (folder? setting-value)
      (configure-controls setting-value {:gui (.addFolder gui setting-key) :open true})

      :else
      (let [{:keys [value min max step options onChange] :or {onChange #(js/console.log "onChange")}} setting-value
            controller (atom {})]

        (swap! state assoc setting-key value)

        (cond
          (not (nil? options))
          (reset! controller (.add gui (clj->js @state) setting-key options))

          (color? value)
          (reset! controller (.addColor gui  (clj->js @state) setting-key))

          :else
          (reset! controller (.add gui (clj->js @state) setting-key min max step)))

        (.onChange @controller #(onChange % @state))))))

(defn configure-controls
  "Multi method for building DATGUI controls"
  ([settings] (configure-controls settings {:width 300 }))
  ([settings options]

   (let [gui (if (:gui options)
               (:gui options)
               (dg/GUI. (clj->js options)))
         state (atom {})]

     (loop [setting (first settings)
            more (rest settings)]

       (if (nil? setting)
         ;; true: finish loop
         (js/console.log "Done")

         ;; false: create controller and continue
         (do
           (create-controller gui setting state)
           (recur (first more) (rest more)))))
     (if (:open options) (.open gui) (.close gui)))))

(comment

  (configure-controls  {"Parent Color" {:value 0 :min 0 :max 100 :step 2 :onChange (fn [v] (js/console.log (str "Hello " v)))}
                     "Color" {"Sphere Color" {:value "#ff0000"}
                              "Square Color" {:value "#00ff00"}
                              "Triangle Color" {:value "#0000ff"}}})
  )




