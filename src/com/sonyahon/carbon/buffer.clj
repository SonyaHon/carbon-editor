(ns com.sonyahon.carbon.buffer
  (:require [clojure.string :as string]))

(defrecord Buffer [text lines point])

(defn empty-buffer
  "Creates an empty buffer"
  []
  (->Buffer "" [{:start 0 :end 0}] 0))

(defn recalculate-lines [text]
  (let [lines (string/split text #"\r?\n" -1)
        offset (atom 0)]
    (into [] (map (fn [line]
                    (let [result {:start @offset :end (+ @offset (count line))}]
                      (swap! offset + (count line) 1)
                      result)) lines))))

(defn append [buffer text]
  (let [new-text (str (:text buffer) text)]
    (assoc buffer
           :text   new-text
           :lines  (recalculate-lines new-text)
           :point  (+ (:point buffer) (count text)))))

(defn delete-from-end [buffer char-count]
  (if (>= (count (:text buffer)) char-count)
    (let [new-text (subs (:text buffer) 0 (- (count (:text buffer)) char-count))]
      (assoc buffer
             :text new-text
             :lines (recalculate-lines new-text)
             :point (max (- (:point buffer) char-count) 0)))
    buffer))

(defn insert [buffer text index]
  (let [[left right] (map #(apply str %) (split-at index (:text buffer)))
        new-text (str left text right)]
    (assoc buffer
           :text new-text
           :lines (recalculate-lines new-text)
           :point  (+ (:point buffer) (count text)))))

(defn remove-backwards [buffer char-count index]
  (let [[left right] (map #(apply str %) (split-at index (:text buffer)))
        new-text     (str (subs left 0 (dec (count left))) right)]
    (assoc buffer
           :text new-text
           :lines (recalculate-lines new-text)
           :point (max (- (:point buffer) char-count) 0))))

(defn- offset->coords [buffer offset]
  (let [lines (map-indexed vector (:lines buffer))
        cursor-line (first (filter (fn [[_ {start :start end :end}]]
                                     (<= start offset end)) lines))]
    (if (some? cursor-line)
      [(- offset (:start (second cursor-line))) (first cursor-line)]
      [0 (count (:lines buffer))])))

(defn get-point-coords [buffer]
  (offset->coords buffer (:point buffer)))

(defn get-point-line
  ([buffer]
   (let [point (:point buffer)]
     (get-point-line buffer point)))
  ([buffer point]
   (let [lines (map-indexed vector (:lines buffer))]
     (first (first (filter (fn [[index {start :start end :end}]]
                             (<= start point end)) lines))))))

(defn get-range
  "Get range of text from a buffer. If range is outside of buffers length
  return a space"
  [buffer start end]
  (if (> end (count (:text buffer)))
    " "
    (subs (:text buffer) start end)))

(defn move-point
  "Moves point accoring to x and y passed. y axis pointing downwards.
  Note that first the x transform is applied, than y transform is applied."
  [buffer x y]
  (let [point (:point buffer)
        new-point-x (+ x point)
        new-point   (if (not= 0 y)
                      (let [point-line (get-point-line buffer new-point-x)
                            new-point-line (+ point-line y)]
                        (if (<= 0 new-point-line (dec (count (:lines buffer))))
                          (let [current-point-line-offset (- new-point-x (:start (nth (:lines buffer) point-line)))
                                {start :start
                                 end   :end}              (nth (:lines buffer) new-point-line)
                                new-point-position        (+ start current-point-line-offset)]
                            (prn new-point-position)
                            (min end new-point-position))
                          new-point-x))
                      new-point-x)]

    (if (<= 0 new-point (count (:text buffer)))
      (assoc buffer
             :point new-point)
      buffer)))

(comment

  (str "asd" "asd")

  (string/split "a s d" #" ")

  (map #(apply str %) (split-at 2 "abcd"))

  (subs "Hello" 0 (count "Hello"))

  (string/split "\n\n" #"\r?\n" -1)

  (get-range (empty-buffer) 0 1)
  (get-range (append (empty-buffer) "Hello") 0 1)

  (get-point-line (empty-buffer))

  69)
