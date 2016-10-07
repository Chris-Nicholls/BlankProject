using Improbable.Unity.Visualizer;
using Position;
using UnityEngine;

namespace Assets.Gamelogic.Position
{
    class QuantizedPositionVisualizer : MonoBehaviour
    {
        [Require]
        private QuantizedPosition.Reader Position;

        private Vector3 _position;

        public void OnEnable()
        {
            SetPosition();
        }

        private void SetPosition()
        {
            _position.x = Position.Data.x * Position.Data.scale;
            _position.y = Position.Data.y * Position.Data.scale;
            transform.position = _position;
        }

        public void Update()
        {
            if (!Position.HasAuthority)
            {
                SetPosition();
            }
        }
    }
}