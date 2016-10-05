using System;
using Improbable.Unity.Visualizer;
using Position;
using UnityEngine;

namespace Assets.Gamelogic.Position
{
    class PositionVisualizer2D : MonoBehaviour
    {
        [Require] private SimplePosition.Reader Position;

        private Vector3 _position;

        public void OnEnable()
        {
            SetPosition();
        }

        private void SetPosition()
        {
            _position.x = (float) Position.Data.position.X;
            _position.y = (float) Position.Data.position.Z;
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