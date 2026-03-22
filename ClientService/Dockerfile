# Stage 1: Copy static files
FROM node:20-alpine AS build
WORKDIR /app
COPY . .

# Stage 2: Serve with nginx
FROM nginx:stable-alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx page
RUN rm -rf ./*

# Copy static files
COPY --from=build /app/Index.html ./index.html
COPY --from=build /app/hospital.html ./hospital.html
COPY --from=build /app/login.html ./login.html
COPY --from=build /app/patient-signup.html ./patient-signup.html
COPY --from=build /app/user-profile.html ./user-profile.html
COPY --from=build /app/doctor-registration.html ./doctor-registration.html
COPY --from=build /app/schedule.html ./schedule.html
COPY --from=build /app/appointment.html ./appointment.html

# Copy nginx config template
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Render sets $PORT — envsubst injects it into nginx config at container start
ENV PORT=3000
CMD ["/bin/sh", "-c", "envsubst '${PORT}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
