using Bot;
using Improbable.Unity.Common.Core.Math;
using Improbable.Unity.Visualizer;
using Player;
using UnityEngine;

namespace Assets.Gamelogic.Movement
{
    class RandomMovement : MonoBehaviour
    {
        [Require] private PlayerValues.Reader Player;

        [Require] private Waypoint.Writer Waypoint;

        public float ArrivalThreshold = 1;

        private Vector3 _velocity;
        private Rigidbody2D _rigidbody;

        public void OnEnable()
        {
            _rigidbody = GetComponent<Rigidbody2D>();
        }

        
        public void FixedUpdate()
        {
            _velocity.x = GetDelta(transform.position.x, Waypoint.Data.next.X, ArrivalThreshold) * Player.Data.speed;
            _velocity.y = GetDelta(transform.position.y, Waypoint.Data.next.Y, ArrivalThreshold) * Player.Data.speed;
            _rigidbody.velocity = _velocity;

            if (_velocity.magnitude == 0 || _rigidbody.velocity.magnitude == 0)
            {
                var waypoint = Random.insideUnitSphere * 100;
                waypoint.z = 0;
                waypoint += transform.position;
                var update = new Waypoint.Update();
                update.SetNext(waypoint.ToNativeVector3f());
                Waypoint.Send(update);
            }

        }

        public float GetDelta(float from, float to, float threshold)
        {
            var delta = to - from;
            if (delta > threshold) return 1;
            if (delta < -threshold) return -1;
            return 0;
        }
    }
}
