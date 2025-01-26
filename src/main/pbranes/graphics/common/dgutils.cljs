(ns pbranes.graphics.common.dgutils
  (:require ["dat.gui" :as dg]
            [cljs.core :as c]))

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
  "Multi method for building DATGUI controls"
  ([settings] (configure-controls settings {:width 300}))
  ([settings options]

   (let [gui (if (:gui options)
               (:gui options)
               (dg/GUI. (clj->js options)))
         state (atom {})]

     (loop [setting (first settings)
            more (rest settings)]

       (if (nil? setting)
         ;; true: stop loop
         nil
         ;; false: create controller and continue
         (do
           (let [setting-key (first setting)
                 key (name setting-key)
                 setting-value (second setting)]

             (cond
               (action? setting-value)
               (do
                 (swap! state assoc setting-key (clj->js setting-value))
                 (.add gui (clj->js @state) (name setting-key)))

               (folder? setting-value)
               (configure-controls setting-value {:gui (.addFolder gui (name setting-key)) :open true})

               :else
               (let [{:keys [value min max step options onChange] :or {onChange #(js/console.log "onChange")}} setting-value
                     controller (atom {})]

                 (swap! state assoc (name setting-key) value)

                 (cond
                   (not (nil? options))
                   (reset! controller (.add gui (clj->js @state) key (clj->js  options)))

                   (color? value)
                   (reset! controller (.addColor gui  (clj->js @state) key))

                   :else
                   (reset! controller (.add gui (clj->js @state) key min max step)))

                 (.onChange @controller (fn [v]
                                          (onChange v state))))))
           
           (recur (first more) (rest more)))))
     (if (:open options) (.open gui) (.close gui)))))

(comment

  (configure-controls  {"Parent Color" {:value 0 :min 0 :max 100 :step 2 :onChange (fn [v] (js/console.log (str "Hello " v)))}
                        "Color" {"Sphere Color" {:value "#ff0000"}
                                 "Square Color" {:value "#00ff00"}
                                 "Triangle Color" {:value "#0000ff"}}}))




