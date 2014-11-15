(ns game3d.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [game3d.test-state :as test-state]
              [game3d.actors.infantry :as infantry]
              [game3d.actors.proto :as proto]
              [cljs.core.async :as async
               :refer [<! >! chan close! sliding-buffer put! alts! timeout]]
              )
    (:require-macros [cljs.core.async.macros :as m :refer [go]])
    (:import goog.History))

;; -------------------------
;; State
(def timer (atom (js/Date.)))
(def canvas (atom nil))
(def engine (atom nil))
(def scene (atom nil))
(def camera (atom nil))
(def prot (atom nil))
(def protskel (atom nil))
(def protanim (atom nil))
(defonce app-state (atom {:text "Hello, this is a page: "}))

(defn get-state [k & [default]]
  (clojure.core/get @app-state k default))

(defn put! [k v]
  (swap! app-state assoc k v))

(defn add-actor []
  (let [i (infantry/Infantry. {:id 2})
        ]
    (proto/model i @scene (fn [meshes particles skeletons] 
                            (let [dude (first meshes)]
                              (set! (.-scaling dude) (BABYLON.Vector3. 0.05 0.05 0.05))
                              (reset! prot dude)
                              (reset! protskel (first skeletons))
                              ((proto/get-animation-fn-for-action i :walk) @scene (first skeletons))
                              )))
    )
)

(defn add-click-chan []
  "adds a channel to capt clicks"
  (def clicks-chan (chan))
  (def ground-clicks-chan (chan))
  (set! (.-onPointerDown @scene) (fn [evnt pickResult]
                                   (go (>! clicks-chan {:event evnt :pick-result pickResult}))))
  (go 
    (loop []
        (let [t (<! clicks-chan)
              res (:pick-result t)]
          (if (= (.-id (.-pickedMesh res)) "ground")
            (>! ground-clicks-chan t))
          )
      (recur)))
  (go (loop []
        (let [t (<! ground-clicks-chan)
              res (:pick-result t)]

          (let [anim (BABYLON.Animation. "protanim" "position" 30 BABYLON.Animation.ANIMATIONTYPE_VECTOR3 )
                posprot (.-position @prot)
                postarget (.-pickedPoint res)
                frames  [{:frame 0 :value posprot}
                        {:frame 100 :value postarget}]
                product (BABYLON.Vector3.Cross. posprot postarget)]
            (.setKeys anim (clj->js frames))
            (.push (.-animations @prot) anim)
            (.lookAt @prot postarget 0 0 0)
            (.beginAnimation @scene @prot 0 100 false)
            )
          (.log js/console res)
        (recur))))
  )

(defn add-floor []
  "adds a floor to scene"
  (let [floor (BABYLON.Mesh.CreateGround. "ground"  32 32 2 @scene)]
    )
  )

(defn add-dome []
  (let [skybox (BABYLON.Mesh.CreateBox. "skyBox" 100 @scene)
        skyboxMaterial (BABYLON.StandardMaterial. "skyBox" @scene)]
    (set! (.-backFaceCulling skyboxMaterial) false)
    (set! (.-diffuseColor skyboxMaterial) (BABYLON.Color3. 0 0 0))
    (set! (.-specularColor skyboxMaterial) (BABYLON.Color3. 0 0 0))
    (set! (.-reflectionTexture skyboxMaterial) (BABYLON.CubeTexture. "assets/cubemap/cubemap" @scene))
    (set! (.-reflectionTexture.coordinatesMode skyboxMaterial) BABYLON.Texture.SKYBOX_MODE)
    (set! (.-material skybox) skyboxMaterial)
    )
  )

;; -------------------------
;; Views
(defn page1 []
  [:div 
   [:canvas {:id "renderCanvas"}] 
   (get-state :text) "Le Page 1"
   [:div [:a {:href "#/page2"} "go to Le page 2"]]
   ])

(defn page2 []
  [:div (get-state :text) "Le Page 2"
   [:div [:a {:href "#/"} "go to Le page 1"]]])

(defn main-page []
  [:div [(get-state :current-page)]])

(defn init-game []
  (let [sphere (BABYLON.Mesh.CreateSphere. "sphere" 10 1 @scene)]
    (set! (.-position.x sphere) -2)
    )
  (add-dome)
  (add-actor)
  (add-floor)
  (add-click-chan)
)

(defn render-loop []
  (.render @scene))

(defn init-scene []
  (reset! canvas (.getElementById js/document "renderCanvas"))
  (reset! engine (BABYLON.Engine. @canvas true))
  (reset! scene (BABYLON.Scene. @engine))
  (reset! camera (BABYLON.FreeCamera. 
                  "camera"
                  (BABYLON.Vector3. 0 4 -10)
                  @scene))
  (.setTarget @camera (BABYLON.Vector3. 0 0 10))
  (.attachControl @camera @canvas)
  (BABYLON.PointLight. "light" (BABYLON.Vector3. 0 5 -5) @scene)
  (.runRenderLoop @engine render-loop)
  (init-game)
  )

(defn init-babylon []
  (if (not= (.-readyState js/document) "complete")
    (.addEventListener js/document "DOMContentLoaded" (fn []
                                                        (if (.isSupported BABYLON.Engine) (init-scene))) false)
    (init-scene))
  )

;; -------------------------

;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (put! :current-page page1)
  (js/setTimeout #(init-babylon) 1000) 
  )

(secretary/defroute "/page2" []
  (put! :current-page page2))

;; -------------------------
;; Initialize app
(defn init! []
  (reagent/render-component [main-page] (.getElementById js/document "app")))

;; -------------------------
;; History
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
;; need to run this after routes have been defined
(hook-browser-navigation!)
